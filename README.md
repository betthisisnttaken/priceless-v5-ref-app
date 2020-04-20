# Priceless API v5 Tutorial Implementation

This code showcases reference implementation of Priceless v5 APIs from: [Mastercard Developers.](https://developer.mastercard.com/product/priceless-api-v5).

## Optional - Installing Markdown Viewer in Google Chrome
- Follow the steps [here](https://imagecomputing.net/damien.rohmer/teaching/general/markdown_viewer/index.html) to view .md files in the browser.
- Paste the README.md file path in the chrome browser.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Frameworks / Libraries used

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Maven](https://maven.apache.org/index.html)

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Requirements
- Java 7 and above
- Set up the [JAVA_HOME](https://explainjava.com/java-path/) environment variable to match the location of your Java installation.
- Set up the [MAVEN_HOME](https://dzone.com/articles/installing-maven) environment variable to match the location of your Maven bin folder.

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## Setup
1.  Open Mastercard Developers website and create an account at [Mastercard Developers](https://developer.mastercard.com).
2.  Create a new project [here](https://developer.mastercard.com/dashboard). Tutorial to create a project can be found [here](https://developer.mastercard.com/pay-with-rewards/documentation/tutorials/create-a-new-api-project/)
3.  Add the `Priceless API v5` API to your project and click continue.
4.  Configure project and click continue.
5.  Download Sandbox Signing Key.
6.  A `.p12` file is downloaded automatically. **Note**: On Safari, the file name will be `Unknown`. Rename it to a .p12 extension.
7.  Copy the downloaded `.p12` file to `src/main/resources` folder in the code.
8.  Open `src/main/resources/application.properties` and configure:
      - `mastercard.api.partnerid` - The Partner ID provided to you by the Priceless team when you were granted access to the sandbox.
      - `mastercard.api.p12.path` - Path to keystore (.p12) file. Since the .p12 is under same resources folder just pass the name of the .p12 here.
      - `mastercard.api.consumer.key` - Consumer key. Copy this from "Sandbox/Production Keys" on your project page
      - `mastercard.api.key.alias` - Key alias. Default key alias for sandbox is `keyalias`.
      - `mastercard.api.keystore.password` - Keystore password. Default keystore password for sandbox project is `keystorepassword`.
9.  Run `mvn clean install` from the root of the project directory.
10. Open the project in your favorite IDE and check the console for output.
 
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     
## Service Documentation

Priceless API v5 service documentation can be found [here](https://developer.mastercard.com/priceless-api-v5/documentation/)

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

