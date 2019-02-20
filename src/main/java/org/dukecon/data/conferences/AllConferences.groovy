package org.dukecon.data.conferences

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * @author Gerd Aschemann <gerd@aschemann.net>
 */
@Slf4j
class AllConferences {

    static baseUrlPlaceHolder = "CONFERENCES_BASE_URL"
    static generatedDirBase = "target/generated"

    Yaml yaml
    def conferences

    public AllConferences() {
        DumperOptions options = new DumperOptions()
        options.setPrettyFlow(true)
        yaml = new Yaml(options)
    }

    // TODO Replace this by generic (all) conferences.yml and/or allow to retrieve different sets
    public void load(String conferenceFilename = "conferences-stable.yml") {
        log.info("Starting to convert file from resource '{}'", conferenceFilename)
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(conferenceFilename)
        conferences = yaml.load(inputStream).grep {entry -> entry instanceof Map}.sort {entry -> entry.id}
    }

    public void list() {
        conferences.each {conference ->
            println "${conference?.id}:${conference?.talksUri?.eventsData}:${conference.talksUri?.speakersData?:''}:${conference.talksUri?.additionalData?:''}"
        }
    }

    private void fetchIfHttp (def conference, String uriField, String filename, File targetDir) {
        String url = conference.talksUri[uriField]
        if (url?.startsWith("http")) {
            File dataDir = new File (targetDir, "data")
            dataDir.mkdirs()
            File confDir = new File (dataDir, conference.id)
            confDir.mkdirs()
            File targetFile = new File (confDir, filename)
            conference.talksUri[uriField] = "${baseUrlPlaceHolder}/data/${conference.id}/${filename}".toString()
            if (targetFile.exists()) {
                log.debug("Targetfile '{}' already exists (skipping)", targetFile)
                return
            }
            log.debug("Fetching '{}' to '{}'", url, targetFile)
            def get = new URL(url).openConnection()
            def getRC = get.getResponseCode()
            if(getRC.equals(200)) {
                targetFile.write(get.getInputStream().getText("Windows-1252"), "Windows-1252")
            } else {
                log.error ("Could not retrieve '{}': {}", url, getRC)
            }
        } else if (url) {
            conference.talksUri[uriField] = "${baseUrlPlaceHolder}/data/${url}".toString()
        }
    }

    public void fetch(String targetDirname = "${generatedDirBase}/htdocs") {
        File targetDir = new File (targetDirname)
        targetDir.mkdirs()

        conferences.each {conference ->
            fetchIfHttp(conference, "eventsData", "events.json", targetDir)
            fetchIfHttp(conference, "speakersData", "speakers.json", targetDir)
            fetchIfHttp(conference, "additionalData", "additionaldata.json", targetDir)
        }
    }

    public void dump(targetDirname = "${generatedDirBase}/templates") {
        File confDir = new File (targetDirname)
        confDir.mkdirs()
        File confFile = new File (confDir, "conferences.yml.template")
        log.debug ("Dumping conferences to '{}'", confFile)
        StringWriter yamlContents = new StringWriter()
        yaml.dump(conferences, yamlContents)
        confFile.write("# This file is generate automatically - DO NOT EDIT\n# ${new Date()}\n" + yamlContents.toString())
    }

    public void generateDockerfile (targetDirname = generatedDirBase) {
        File targetDir = new File(targetDirname)
        String dockerFileContents = """FROM dukecon/dukecon-httpd-base:latest
MAINTAINER Gerd Aschemann <gerd@aschemann.net>

RUN apk update && \
    apk add perl-cgi && \
    perl -p -i -e 's/#LoadModule cgi/LoadModule cgi/go' /usr/local/apache2/conf/httpd.conf

RUN /bin/rm -f /usr/local/apache2/cgi-bin/*

ADD cgi-bin /usr/local/apache2/cgi-bin
RUN chmod +x /usr/local/apache2/cgi-bin/*

"""
        conferences.each {conference ->
            dockerFileContents += """ADD htdocs/data/${conference.id} /usr/local/apache2/htdocs/data/${conference.id}
"""
        }

        dockerFileContents += """
ADD templates /usr/local/apache2/templates
"""
        File dockerFile = new File(targetDir, "Dockerfile")
        dockerFile.write(dockerFileContents.toString())
    }

    public static void main (String[] args) {
        AllConferences allConferences = new AllConferences()
        allConferences.load()
        allConferences.list()
        allConferences.fetch()
        allConferences.dump()
        allConferences.generateDockerfile()
    }

}
