#Alfresco SDK Supplement

This project provides a series of command line utilities to assist with some common development tasks.

### Version
1.0

### Utilities available
Rename, and Remove Examples

### Todo
Integrate the [Technical Validation Tool]
Add utilities for creating sub-modules; webscripts, etc.
Also factor in tools and utilities that exist in current 
Alfresco Consulting projects.



### Usage
Usage: cmd [options]
  
Available commands are:
Alfresco SDK development tools
	Usage: cmd [options] 

	Available commands are:
	NAME
		Rename - rename an amp module
	SYNOPSIS
	rename or rn [--rename.from=repo-amp] --rename.to=new-name
	DESCRIPTION
		The rename command is used to rename an amp file from one name to another.
		This rename function will also update the appropriate pom.xml and
		and files needed to bootstrap the project into the all-in-one sdk.
	OPTIONS
		--rename.from
			Existing Maven module to be renamed
		--rename.to
			The new name to rename the maven project to.


	NAME
		RemoveExamples - Remove example files
	SYNOPSIS
	RemoveExamples or re
	DESCRIPTION
		The remove examples command will remove the example hello-world stubs
		that are delivered in the all-in-one sdk.
	OPTIONS
		-none-


	NAME
		TestTruststore - Test an Alfresco truststore
	SYNOPSIS
	TestTruststore or tt --tt.host=example.com[:port] --tt.store=path/to/store --tt.pass [--tt.trust=true][--tt.storetype=JCEKS] [--tt.backup=/path/to/backup] [--tt.writeto=keystore]
	DESCRIPTION
		Test an Alfresco JCEKS truststore against a host. This helps in validating
		that the appropriate CA is in the truststore.
		Also note that this command assumes that the truststore is accompanied by
		ssl-truststore-passwords.properties.
	OPTIONS
		--tt.host[:port]
			Host name to test. For example: www.alfresco.com:8443. Port 443 is default.
		--tt.store
			JCEKS Trust store file path. 
		--tt.pass
			Password for the trust store
		--tt.trust
			If this parameter is set to true (default false),then the public cert
			of the host will be download into the truststore.
		--tt.backup
			Directory to backup truststore. Default is to backup in same directory as keystore
		--tt.writeto
			Folder or File to write store file to. Default is to overwrite keystore
		--tt.storetype
			Keystore type. Default JCEKS.


	ENVIRONMENT
		Alfresco Maven SDK 2.1
		Alfresco 5.x





[Technical Validation Tool]:https://github.com/AlfrescoLabs/technical-validation

