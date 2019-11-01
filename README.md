# aarc-ssh-portal
The AARC SSH-portal is a client portal to the AARC Master Portal which provides
a user interface for the SSH public key upload API from the
[AARC Master Portal](http://github.com/rcauth-eu/aarc-master-portal).  
It is based on a customised version of the
[OA4MP](https://github.com/rcauth-eu/OA4MP).

## Implementation
The SSH portal is an OA4MP client implementation. It talks to the 
Master Portal's [sshkey endpoint](https://wiki.nikhef.nl/grid/Master_Portal_sshkey_endpoint).
It runs as a tomcat servlet inside the same tomcat container as the Master Portal itself.

## Compiling

1. You first need to compile and install the two RCauth-adapted dependency
   libraries 
    1. [security-lib](https://github.com/rcauth-eu/security-lib)
    2. [OA4MP](https://github.com/rcauth-eu/OA4MP)
   
   Make sure to use the _same_ version (branch or tag) for both the
   security-lib and OA4MP components.  
   For the **0.2** series of the aarc-ssh-portal, you must use the
   **4.2-RCauth** versions.
   
2. Checkout the right version of the aarc-ssh-portal.

        git clone https://github.com/rcauth-eu/aarc-ssh-portal
        cd aarc-ssh-portal

        git checkout v0.2.1
        cd ssh-portal

3. Build the ssh-portal's war file

        mvn clean package

   After maven has finished you should find the `.war` file in the target
   directory:

        aarc-ssh-portal/ssh-portal/target/sshkey-portal.war

## Other Resources

Background information:
* [RCauth.eu and MasterPortal documentation](https://wiki.nikhef.nl/grid/RCauth.eu_and_MasterPortal_documentation)
* [Master Portal's sshkey endpoint](https://wiki.nikhef.nl/grid/Master_Portal_sshkey_endpoint)
* [Ansible scripts for the Master Portal](https://github.com/rcauth-eu/aarc-ansible-master-portal)

Related Components:
* [RCauth.eu Delegation Server](https://github.com/rcauth-eu/aarc-delegation-server).
* [AARC Master Portal](https://github.com/rcauth-eu/aarc-master-portal)  
* [Demo VO portal](https://github.com/rcauth-eu/aarc-vo-portal)  
  this component can run inside the master portal's tomcat container,
  providing a demonstration client portal to the Master Portal.
