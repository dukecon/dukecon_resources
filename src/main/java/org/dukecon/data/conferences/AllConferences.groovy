package org.dukecon.data.conferences

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

import java.nio.charset.StandardCharsets

@Slf4j
class AllConferences {

    def conferences

    public void load(String conferenceFilename = "conferences.yml") {
        log.info("Starting to convert files")
        Yaml yaml = new Yaml();
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

    private void fetchIfHttp (String url, String filename, File targetDir) {
        if (url?.startsWith("http")) {
            File targetFile = new File (targetDir, filename)
            if (targetFile.exists()) {
                log.debug("Targetfile '{}' already exists (skipping)", targetFile)
                return
            }
            log.debug("Fetching '{}' to '{}'", url, targetFile)
            def get = new URL(url).openConnection()
            def getRC = get.getResponseCode()
            if(getRC.equals(200)) {
                targetFile.write(get.getInputStream().getText("Windows-1252"), "Windows-1252")
//                targetFile.write(get.getInputStream().getText(StandardCharsets.ISO_8859_1.displayName()), StandardCharsets.UTF_8.displayName())
            } else {
                log.error ("Could not retrieve '{}': {}", url, getRC)
            }
        }
    }

    public void fetch(String targetDir = "target/generated/data") {
        File dataDir = new File (targetDir)
        dataDir.mkdirs()

        conferences.each {conference ->
            File confDir = new File (targetDir, conference.id)
            confDir.mkdirs()
            fetchIfHttp(conference.talksUri.eventsData, "events.json", confDir)
            fetchIfHttp(conference.talksUri.speakersData, "speakers.json", confDir)
            fetchIfHttp(conference.talksUri.additionalData, "additionaldata.json", confDir)
        }
    }

    public static void main (String[] args) {
        AllConferences allConferences = new AllConferences()
        allConferences.load()
        allConferences.list()
        allConferences.fetch()
    }

}