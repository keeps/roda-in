# RODA-In

RODA-in is a tool specially designed for producers and archivists to create Submission Information Packages (SIP) ready for to be submitted to a Open Archival Information System (OAIS). The tool creates SIPs from files and folders available on the local file system. 

In version 2 we revolutionized the way SIPs are created to satisfy the need for mass processing of data. In this version you can create thousands of valid SIPs with just a few clicks, complete with data and metadata.

The tool includes features such as:
* Create, load and edit a classification schemas
* Automatic association of files/folders to SIPs
* Automatic association of metadata to SIPs
* Definition of metadata templates
* Support for various metadata formats (EAD, DC, etc.)
* Creation of SIPs of unlimited size
* Export SIPs in various formats: [BagIt](https://github.com/LibraryOfCongress/bagit-java) and [E-ARK](http://www.eark-project.com/resources/project-deliverables/51-d33pilotspec)


## Download and installation

Download the [latest release here](https://github.com/keeps/roda-in/releases).

To use RODA-In you must at least have [Java 8](https://www.java.com/en/download/).

## Usage

The basic workflow of the application is:

1. Choose the root directory of the file system (panel on the left).
2. Choose the classification scheme (panel on the center). There's two options:
  - Load a classification scheme. You can obtain a classification scheme from RODA, for example.
  - Create a classification scheme. 
3. Drag files/directories from the left to the center panel
4. Choose the type of association and metadata desired. This will have impact on the number and structure of the SIPs created.
5. (Optional) Inspect the created SIPs, edit metadata and content.
6. Export the SIPs to a directory

To select more than one file/directory you can press SHIFT or CTRL.

The following actions are supported to edit the classification scheme:
* Add node
* Edit the title and the description level of the node
* Move node (using drag and drop)
  - Change parent
  - Move to the root of the tree
* Remove node

## Tutorials

Will be available soon...

## Installation from source code 

1. Make sure you have installed Java 8 and Maven.
2. Clone the repository by issuing the command XXX
3. Install by running the following command:
4. 
```
mvn install
```

That's it! A jar file will be created on the folder XXX. To run it, just double click on the file or run the following command on the console:

```
java -jar roda-in-app.xxx.yyy.jar
```


## Contributing

[![Build Status](https://api.travis-ci.org/keeps/roda-in.png?branch=master)](https://travis-ci.org/keeps/roda-in)

[Maven](https://maven.apache.org/) is required to build RODA-in.

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## License

LGPLv3
