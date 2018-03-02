# Setting Up the Jenkins Slave on OpenStack

To setup a Jenkins slave on OpenStack follow the steps below.

1. Create an Ubuntu instance on OpenStack
2. Replace the following in the setup-slave.sh with the appropriate.
    - Server IP/Hostname of Jenkins master instance

      `server-ip-or-hostname`

    - Jenkins Master URL

      `jenkins-master-url`

3. Execute setup-slave.sh

    `./setup-slave.sh`
    
    To retry specific number of times use the parameter -r when executing the script
    
     `./setup-slave.sh -r 3`

4. Go to Manage Jenkins > Manage Nodes > `<openstack-node-name>`

    Copy the command given to launch the slave in headless mode and run on the slave machine with `nohup` as shown below.

    `nohup <command-provided-in-jenkins-master-to-launch-the-slave>`

_**Note: The slave.jar is already downloaded to the working directory from the  setup-slave.sh**_
