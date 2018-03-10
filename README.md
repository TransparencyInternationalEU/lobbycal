# About lobbycal

Please also refer [to the documentation as PDF](https://github.com/TransparencyInternationalEU/lobbycal/blob/master/doc/How%20to%20install%20LobbyCal%20and%20FAQ.pdf). 

lobbycal helps groups to publish their meetings with lobbyists. 

Each user sends a cc: email of the outlook / google calendar or other event invitations to the server component. 

Via the wordpress plugin, the typo3 extension or on a static html page, the meetings of a user can be embedded on any website. 

Only past meetings will be displayed, unless otherwise configured in the user account on the lobbycal server.

## QUESTIONS

### things you need to change before running

After you checked out the vanilla lobbycal codebase, scan the file tree for files like 'main-sample.html'
Rename those files to main.html before launching the maven build. 

### Running
After you configured the database connection in application[-sample].yml you can run 
´´´mvn spring-boot:run´´´ to start the server. 

### Parsing of email subjects
 
The event summary field (VEVENT:SUMMARY) controls the event details stored in lobbycal.
 
lobbycal expects the following elements in an event summary email subject.

STH: Partner: Title TransparencyRegisterID #tag1 #tag2 * comment

* Comment: dismiss anything after the first '*'
* Tags: cut out any #tag1, #tag2 etc. and use them as tags for the meeting
* STH: Different webmailers or groupwares such as outlook or gmail handle invitations differently. In order to have generic and clear rule on how to deal with that variation, lobbycal dissmisses  the STH part, if an email subject contains two or more colons ':'
* Partner:  Only the last part between two or more colons is interpreted as the partner
* The subject part between the last ':' and '*' contains in arbitrary order
** Title
** TransparencyRegisterID: if string can be identified in the remaining subject that matches the pattern [10-12 digits]-[1-2 digits], it is treated as transparency register id and the title is abbreviated, accordingly



The position of title, transparency register ID and tags /within the last ':' and first '*' is arbitrary. 

### Alias
* we expect the personalized alias to be used in one of the CC addresses. other fields are not examined 

## Processing meeting invitations via email
### Control via Webmail
Only those emails are processed that are located in the INBOX folder and have the UNSEEN flag. 
Reprocessing of the original invitation can be triggered by marking an email as unread.


### Ownership
 
all allowed submitters are treated the same: if multiple appear in the CC of an invitation email, the first match will be taken as the submitter
same applies to the alias check: the first match of the active aliases is used

### Partner entries
Partner entries will be shared. Curation of entries is admins responsibility.
The first time an ID comes in it will be stored in the database. 
Partner ID rules over Partner name. 
Subsequent meeting with the same partner ID will be assigned the existing partner entry in the database. 
Partners are referenced, which means, if a partner entry is changed, i.e. name overwrite, this assignment will take effect on all meetings related to this partner id.

Currently no remote lookup takes place.


# Technical information 

## Installation requirements

The application has a small resource consumption and should run fine on any machine with 2 GB RAM

## Required software

* lobbycal server java based. The latest JDK v8 must be installed
* The persistence layer is driven by a MySQL Database. Database credentials need to be set in `application.properties`. We recommend MariaDB for better performance.
* Apache maven is required during the build process


## Building from source 
 
 * `mvn clean package -Pprod -Dmaven.test.skip=true -U`
 * `mvn package -Pprod -Dmaven.test.skip=true`

## Running
 
`java -jar lobbycal-3.1.8.war --spring.profiles.active=prod`

## Security
* client side js validation / access restriction can be circumvented with some technical background. 
* server side security / access rules is configured in eu.transparency.lobbycal.config.SecurityConfiguration.


# Access to the API from 3rd party sites

You might want to use the nice out-of-the-box API to integrate your app into existing websites. 
One approach could be to use [Knockout](https://github.com/knockout/knockout) to bind your entities with ease and render the entity on e.g. your blog, being served from your jhipster app. 
two aspects need to be changed in standard jhipster in order to use the API from simple HTML/AJAX clients:

1. configure the `SecurityConfiguration`
1. tell the browsers of the users that visit the 3rd party site that uses the API that your app permits the origin of that request.

## SecurityConfiguration
In method `SecurityConfiguration.configure(HttpSecurity http)` , add new directives as needed in the `.and().authorizeRequests()` part, e.g. `.antMatchers("/api/_search/meetings/**").permitAll()            .antMatchers("/api/_search/meetings").permitAll()`   

Of course you can make use of `.hasAuthority()` and `.authenticated()` to make your thinst-client a bit more dynamic. 



## Adapt response headers 


Therefore you need to add the following directives to `CsrfCookieGeneratorFilter` :
In method `doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException`

`response.addHeader("Access-Control-Allow-Origin", "*");`
`response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");`
`response.setHeader("Access-Control-Max-Age", "86400"); // 24 Hours`
`response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, x-auth-token");`



## Show meetings of all users

After registration you should have received an email with your personal API URL.

It should be similar to 

`http://our-lobbycal-server.org/api/meetings/dt/100`

To display all meetings published by that lobbycal server instance, simply remove the id at the end of the URL

`http://our-lobbycal-server.org/api/meetings/dt`

For wordpress, this is done at the plugins settings page /wp-admin/options-general.php?page=lobbycal2press
 
## Show meetings of selected users

To display meetings of selected users, simply add ids of these users at the end of the URL, separated by comma.

`http://our-lobbycal-server.org/api/meetings/dt/42,23`

For wordpress, this is done at the plugins settings page /wp-admin/options-general.php?page=lobbycal2press 
# Deactivation  & Deletion policy

If a user is deactivated, emails related to the user account or one of his submitters are no longer processed. 
Related meetings are still displayed.

Is a user gets deleted, his aliases, meetings and submitters will also get deleted. 

 


## DEVELOPPER NOTES


* the mvn compile liquibase:diff goal runs against the db - the resulting changelog will look different, if you wipe your db beforehand. If you run it without your classes changed, the changelog should be empty. In order to get a clean changelog, wipe you db before compiling an repopulate it, then run mvn compile liquibase:diff  


* when doing so, make sure the liquibase config in pom.xml matches the one from application_dev.yml

* mind the package name for model classes in pom.xml! 
