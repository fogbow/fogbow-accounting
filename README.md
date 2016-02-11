# fogbow-accounting

## Installation

### requirements
* NGINX web server to run the frontend application
* NPM (https://www.npmjs.com/) and bower (http://bower.io/) to resolve dependencies of frontend
* JRE 1.7+ to run the backend application
 
### Get the code from github
$ git clone https://github.com/fogbow/fogbow-accounting.git

### Install the dependencies of the frontend with npm
$ cd fogbow-accounting/frontend
$ npm install

### configure the NGINX
* copy the file in fogbow-accounting/nginx/fogbow-accounting to the /etc/nginx/sites-available
* create a link to this file into the directory /etc/nginx/sites-enabled
* edit the file to set the proper path of the frontend app
* restart the NGINX: $ sudo service nginx restart
 
### start the backend app
* cd fogbow-accounting/webapp
* rename the accounting.conf.example to accounting.conf
* change the configuration file to set the fogbow-manager url
* $ start the webapp bash bin/start
 
Now you can test the application using the servername configured in the nginx file

