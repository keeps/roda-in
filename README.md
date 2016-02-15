# RODA-In

RODA-in is a tool to help producers and archivists create Submission Information Packages (SIP) using the file system as the working area. 

In version 2 we revolutionized the way SIP are created to satisfy the need of mass processing data. In the new version you can create thousands of valid SIP in just a few clicks, complete with metadata.

The tool has features such as:
* Create, load and edit a classification scheme
* Automatic association of files/folders to SIP(s)
* Automatic association of metadata to SIP(s)
* Creation of metadata templates
* Support for all metadata formats (EAD, DC, etc.)
* Creation of SIP with unlimited size
* Export SIP in two formats: [BagIt](https://github.com/LibraryOfCongress/bagit-java) and [E-ARK](http://www.eark-project.com/resources/project-deliverables/51-d33pilotspec)

## Installation

```
mvn install
```

## Usage

Download the [latest release here](https://github.com/keeps/roda-in/releases).

To use RODA-In you must at least have [Java 8](https://www.java.com/en/download/).

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
