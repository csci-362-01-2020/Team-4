# Spatiotemporal Epidemiological Modeler (STEM) Project Testing

- [Project Background](#project-background)
- [Instillation](#instillation)
- [Usage](#usage)
- [Authors and Acknowledgements](#authors-and-acknowledgements)
- [License](#license)


## Project Background

This project is the result of a semester-long project for a Software Engineering class. Throughout the course, we have learned about software engineering expectations and standards, development process models, past and current frameworks and views, and ethical issues related to the practice of software engineering. More information about our experiences in the course, as well as a report documenting our work on this project in more detail, can be found on our [team wiki](https://github.com/csci-362-01-2020/Team-4/wiki).

This project utilizes the H/FOSS (Humanitarian / Free and Open Source Software) project called The Spatiotemporal Epidemiological Modeler (STEM). From the [STEM wiki](https://www.eclipse.org/stem/), "The Spatiotemporal Epidemiological Modeler (STEM) tool is designed to help scientists and public health officials create and use spatial and temporal models of emerging infectious diseases. These models can aid in understanding and potentially preventing the spread of such diseases." In short, the project provides a framework so that developers and researchers can model simulations ranging from a local to a global scale to determine what policies and strategies may help prevent the spread of various diseases.

Our project works with STEM in order to test segments of the STEM source code and return a test report based on the results. This guide will assume using a Debian-based version of Linux like Ubuntu or Mint with Bash, or a Debian-based Linux Subsystem (also with Bash).


## Installation

Running our project does not require full instillation of Eclipse or STEM, although directions for setup will be provided at the end of this section.

To run this testing project, all that is required is to make a local copy of our repository, navigate to "TestAutomation", and the command to run the project is ```./scripts/runAllTests.sh```

All .jar files needed to run our specific test cases are included in the package structure at ```TestAutomation/project/dependencies/``` and further .jar files could be added as necessary to include more testing drivers. Test drivers can be found at ```TestAutomation/project/src/org/eclipse/stem/test/driver/``` with a sub-directory provided for each class tested.

---

STEM can be downloaded [here](https://www.eclipse.org/stem/downloads.php) and a setup tutorial for developers can be found [here](https://wiki.eclipse.org/STEM_Eclipse_Setup). You do not need to download STEM independently if you follow the developer setup guide, steps will be provided. To work with the STEM code in Eclipse, you will need to install Java version 11 or later as well as Java 8 (aka JDK 1.8). The latest version of Eclipse (at the time of writing) can be found [here](https://www.eclipse.org/downloads/packages/installer) and the commands to check your current Java version and to install the required Java version are shown below.


To check your current Java version:
```
java -version
```
You want this to be Java version 11 or later, or the latest versions of Eclipse will not run.


To install the latest Java version:
```
sudo apt install default-jdk
```

To install Java version 8:
```
sudo apt install openjdk-8-jdk
```


## Usage

### Common Practices:

- We have maintained a similar file structure as the original STEM project within our own file structure as specified in Chapter 2 of this report. This does result in extra directories between a root and a target but serves to maintain consistency with the orignal project and thus allow for more flexibility in future updates and further implmentation of this automated testing framework.


### How to run:

1. Navigate to TestAutomation
2. Run the script with ./scripts/runAllTests.sh
3. The report document will be in TestAutomation/reports, but it will automatically be pulled up in Firefox
4. Close the document to stop the process


## Fault Injection

Each of the faults can be injected in .java files under the project/src folder. We have included the package structure in order for you to navigate to the .java files.

### org.eclipse.stem.graphgenerators.impl.PajekNetGraphGeneratorImplOld

line 1022: remove "- x" (will cause all test cases to fail)

### org.eclipse.stem.analysis.automaticexperiment.NelderMeadAlgorithm

Line 44: Change 0.5 to 0.1 (will cause test case 004 to fail)

### org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl

Line 911: Change Math.abs(2.0*(d1-d2)/(d1+d2)) to Math.abs(2.0*(d1+d2)/(d1-d2)) (will cause test cases 007 and 008 to fail)

## Authors and Acknowledgements






## License

[Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/)
