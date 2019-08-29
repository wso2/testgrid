import jenkins.model.Jenkins

def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials( 
     com.cloudbees.plugins.credentials.common.StandardUsernameCredentials.class,
     Jenkins.instance,
     null,
     null
    );

def creds_2 = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials( 
     org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl.class,
     Jenkins.instance,
     null,
     null
    );

def creds_3 = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials( 
     org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl.class,
     Jenkins.instance,
     null,
     null
    );
def count=creds.size()+creds_2.size()+creds_3.size()
println "${count}"



