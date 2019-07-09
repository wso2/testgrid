
## Amazon AMI (Amazon Machine Image) creation scripts for Testgrid integration test execution

If you want to do changes to the EC2 instance where integration tests are running, you can do that from here. 
Usually, you would want to do changes here for reasons,

1. Add a new JDK version to the existing mix of JDKs.
2. Add a new AMI for a new operating system version you want to test
3. Add a new jdbc sql driver or driver version.

There are three files here,

1. `init.sh` - This is the script you need to from your local machine to initiate the AMI burning process.
This contain the current AMI image IDs we are using, and Packer invocation logic. Packer is a standard tool for 
burning amazon machine images.
`bash init.sh`

2. `package-conf.json` - This is the packer definition file. This contain references to AMIs (via env vars), 
what shell script to run to populate the AMI (config.sh), etc.

3. `config.sh` - This is the shell script Packer will run to populate the AMI it wants to create. 
This contain instructions to,
* Install OS packages via APT/YUM. This currently support Ubuntu, CentOS 7, RHEL 7, Windows 2016 (function name: #installPackage)
* Install JDKs (function name: #installJDKs)
* Install Maven (function name: #installMaven)
* Install SQL clients (oracle, mysql, mariadb, sqlserver)  (function name: #installMaven)
* Install SQL jdbc drivers (function name: #downloadJDBCDrivers)

## How to add changes to an existing AMI

We use Packer for this purpose. Packer uses `config.sh` shell script to populate base AMIs that only contain 
the bare operating system.

1. Download and install packer from https://www.packer.io/intro/getting-started/install.html#precompiled-binaries

2. Open config.sh and do the changes if you wish to see in the AMI.
Note: We keep large libraries like JDKs in a S3 location (s3://testgrid-resources/packer/). 
You can upload the new JDK you want also to here. This is done for convenience and easier re-producibility purposes.
Otherwise, new packer users will have to manually download all the JDKs and place them in local file system.

3. Run init.sh. No command-line arguments needed.

```
$> bash init.sh                                        
Select the OS of the AMI:
        1 - Ubuntu 18.04
        2 - CentOS 7
        3 - Windows 2016
        4 - RHEL 7.4
> 1

Downloading Ubuntu AMI resources from S3 (testgrid-resources/packer directory)
download: s3://testgrid-resources/packer/Unix/jce_policy-8.zip to resources/jce_policy-8.zip
download: s3://testgrid-resources/packer/Unix/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm to resources/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm
download: s3://testgrid-resources/packer/Unix/ojdbc8.jar to resources/ojdbc8.jar
download: s3://testgrid-resources/packer/Unix/perf_monitoring_artifacts.zip to resources/perf_monitoring_artifacts.zip
download: s3://testgrid-resources/packer/Unix/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm to resources/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm
download: s3://testgrid-resources/packer/Unix/jdk-8u181-linux-x64.rpm to resources/jdk-8u181-linux-x64.rpm
download: s3://testgrid-resources/packer/Ubuntu/resources.txt to resources/resources.txt
resources/ojdbc8.jar found.
resources/oracle-instantclient12.2-basic-12.2.0.1.0-1.x86_64.rpm found.
resources/oracle-instantclient12.2-sqlplus-12.2.0.1.0-1.x86_64.rpm found.
Starting packer..
==> amazon-ebs: Prevalidating AMI Name: TestGrid-Ubuntu-18.04-2019-07-09-1562657897
==> amazon-ebs: Launching a source AWS instance...
==> amazon-ebs: Adding tags to source instance
==> amazon-ebs: Waiting for instance (i-0122828f6e4bfe1db) to become ready...
==> amazon-ebs: Waiting for SSH to become available...
==> amazon-ebs: Connected to SSH!
==> amazon-ebs: Uploading resources => /home/ubuntu/
==> amazon-ebs: Provisioning with shell script: ./config.sh
...
==> amazon-ebs: Stopping the source instance...
==> amazon-ebs: Waiting for the instance to stop...
==> amazon-ebs: Creating the AMI: TestGrid-Ubuntu-18.04-2019-07-09-1562657897
    amazon-ebs: AMI: ami-0531b45841e61d270
==> amazon-ebs: Waiting for AMI to become ready...
==> amazon-ebs: Terminating the source AWS instance...
Build 'amazon-ebs' finished.
==> Builds finished. The artifacts of successful builds are:
--> amazon-ebs: AMIs were created:
us-east-1: ami-0531b45841e61d270
```

This execution takes 20-30 minutes to complete.

3. Your hotly baked new AMI is ready. You can find the new AMI ID at the end of the packer logs.

4. Now, we need to make this the default AMI for the given OS, such that Testgrid will use your AMI for future Testgrid
integration test runs. 
This is done via the AMI tags (specifically the 'AGENT_READY' boolean tag.). The new AMI will have all the tags with 
correct values, it's just that we need to remove the tags from the older AMIs. A minor nuisance.

Now, your AMI is ready. You can head over to Testgrid jenkins, trigger a intg test build, 
and see how it uses the AMI you created.

## Development Notes

### Notes on practical dev work when you Add/Modify/Remove content from existing Testgrid AMIs

Following lists some practical information that is useful for testing your changes in config.sh.

1. Create an instance of the OS you need from OpenStack (TestGrid has its own account. Use the DIY app to request an instance.)
2. Try out how to do the configuration from a script avoiding(solving) external user-inputs. 
    Ex 1: Say you need to download a lib from internet and install it in CentOS;
       You can use _CURL_ and download the file.
       You can use *sudo yum install* and install the file. (But this will request a user-input which has to be answered “Y”)
       To solve that, you can use *`sudo yum -y install <file>`*

     Ex 2: Let’s say the file that you need to download is not directly accessible, you have to go to their website, login / fill a form and then only the file is available;
        In TestGrid S3, there are packer resource directories that can be used to this kind of requirements. What you can do is, download the lib/file to your local machine using your browser and then upload to the relevant packer resource directory in S3.

Packer resource directories are as;
Ubuntu →  resources only used when configuring Ubuntu
CentOS → resources only used when configuring CentOS
Unix → resources used when configuring both Ubuntu & CentOS, and other Linux dists
Windows →  resource used when configuring Windows
Common → resource used across all the OSes.
When creating the AMI, all the relevant S3 directories (eg: if CentOS ⇒ Common, Unix, CentOS directories) will copied to the environment, so that from the script you can directly refer the files (Files will be copied to home/<ssh_user>/resource/)

![Testgrid S3 location](https://user-images.githubusercontent.com/936037/60871584-ade41480-a250-11e9-8a2a-c7b3573f4f17.png)


So you can call 
  `sudo yum -y localinstall resource/<file>.rpm`
  
https://s3.console.aws.amazon.com/s3/buckets/testgrid-resources/packer

3. Once you have the script that can automatically configure the environment, create a EC2 instance from the existing TestGrid AMI.
Run your script there (you may have to manually scp the files which you hope to add to S3 in this case) and verify its successful.

4. Add the changes into the packer resource→ config.sh file.
	config.sh file is written for both Ubuntu and CentOS cases. So you have to add the commands to relevant case.
	If you have added resources to S3 directories and hoping to use, then you have to update the resources.txt file in relevant OS-specific directory in S3. (At AMI creation, it will check all the resources listed in the resource.txt are available before continuing as a verification step)

5. Run init.sh

[Important] You should only build the AMI using the packer with the updated script. No manually build AMIs now onwards...



