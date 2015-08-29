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
rename or rn [--rename.from=repo-amp] --rename.to=new-name

    The rename command is used to rename an amp file from one name to another.
    This rename function will also update the appropriate pom.xml and
    and files needed to bootstrap the project into the all-in-one sdk.
   
RemoveExamples or re

    The remove examples command will remove the example hello-world stubs
    that are delivered in the all-in-one sdk.




[Technical Validation Tool]:https://github.com/AlfrescoLabs/technical-validation

