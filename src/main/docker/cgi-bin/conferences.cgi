#!/usr/bin/perl -w

use strict;
use warnings;

use CGI;
use IO::File;

use constant DEFAULT_BASE_DIR       =>  "/usr/local/apache2";
use constant TEMPLATE_FILE_NAME     => "templates/conferences.yml.template";

my $templateDir = $ENV{BASE_DIR} || DEFAULT_BASE_DIR;
my $templateFilename = $templateDir . "/" . TEMPLATE_FILE_NAME;
my $template = new IO::File ($templateFilename)
  or die "Cannot open template '" . $templateFilename . "'";

print CGI::header();

my $baseUrl = "$ENV{REQUEST_SCHEME}://$ENV{HTTP_HOST}";

while (my $line = $template->getline()) {
    $line =~ s/CONFERENCES_BASE_URL/${baseUrl}/o;
    print $line;
}

