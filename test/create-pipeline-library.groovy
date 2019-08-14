import hudson.model.*
import jenkins.model.*
import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever

final Jenkins jenkins = Jenkins.getInstance()

GitSCMSource librarySource = new GitSCMSource("pipeline-library", "https://github.com/redhat-cop/pipeline-library.git", "", "*", "", true)

LibraryConfiguration config = new LibraryConfiguration("pipeline-library", new SCMSourceRetriever(librarySource))
config.setAllowVersionOverride(true)
config.setImplicit(false)
config.setDefaultVersion("master")

List<LibraryConfiguration> configurations = new ArrayList<LibraryConfiguration>()
configurations.add(config)

GlobalLibraries globalLibraries = GlobalLibraries.get()
globalLibraries.setLibraries(configurations)

jenkins.save()