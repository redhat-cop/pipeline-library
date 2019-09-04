import jenkins.model.*
import hudson.model.*
import hudson.plugins.sonar.*
import hudson.tools.*
import hudson.util.Secret
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

def instance = Jenkins.getInstance()

def SONAR_URL
def SONAR_TOKEN

def stringCredentials = new StringCredentialsImpl(CredentialsScope.GLOBAL, "sonartoken", null, Secret.fromString("${SONAR_TOKEN}"))

SystemCredentialsProvider systemCredentialsProvider = SystemCredentialsProvider.getInstance()
systemCredentialsProvider.getStore().addCredentials(Domain.global(), stringCredentials)
systemCredentialsProvider.save()

SonarInstallation sonarInstallation = new SonarInstallation("sonar", "${SONAR_URL}", "sonartoken", null, null, null, null, null)

SonarGlobalConfiguration sonarGlobalConfiguration = instance.getDescriptor(SonarGlobalConfiguration.class)
sonarGlobalConfiguration.setInstallations(sonarInstallation)
sonarGlobalConfiguration.migrateCredentials()
sonarGlobalConfiguration.save()

instance.save()