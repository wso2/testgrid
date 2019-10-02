import jenkins.model.Jenkins

def username_credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
     com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
     Jenkins.instance,
     null,
     null
    );

def string_credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
     org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl.class,
     Jenkins.instance,
     null,
     null
    );

def file_credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
     org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl.class,
     Jenkins.instance,
     null,
     null
    );

def credentials_count = username_credentials.size() + string_credentials.size() + file_credentials.size()
println "${credentials_count}"
