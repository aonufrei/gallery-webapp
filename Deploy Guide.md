# Spring Boot Application to EC2 deployment guide

I developed a Gallery web application using Spring Boot, Amazon S3 and MySQL. So now I wish to deploy my project to the EC2 server and make it available to the web.

I'll follow these steps:
1. Setup EC2 Instance
2. Install jre and nginx
3. Build Spring Boot application and copy it to ec2 via ssh
4. Configure startup script with environment variables
5. Adjust nginx settings
6. Adjust systemd and run the app


## 1. Setup EC2 Instance

Navigate to the EC2 page of the AWS Console and press the "Launch instance" button.

I want my instance to run Ubuntu, so I select the Ubuntu OS Image.

Select "Allow HTTPs traffic from the internet" and "Allow HTTP traffic from the internet" checkboxes so users can access your web service in the future.

Also I create the key pair to access the instance later. I name it `gallery-webapp-pair.pem`. The key pair file must be installed automatically.

Other configs can remain default.

Now press the "Launch instance" button. New instance should appear on the instances page.

There are multiple ways to connect to the instance. Press the "Connect" button on the instance page to see available options from amazon. I prefer connecting using ssh and terminal.

In my case, the command to connect to the server via ssh looks like this:
```
ssh -i gallery-webapp-pair.pem ubuntu@ec2-52-202-205-64.compute-1.amazonaws.com
```

Run this command to enter the instance. Now you're ready to setup the environment.

## 2. Download jre and nginx

First of all, we need to install jre to be able to run the java project. I use java 11 in my project, so I'll install jre using these commands:
```
sudo apt update
sudo apt install default-jre
```
Now we need to install nginx, to make our app visible to the web:
```
sudo apt install nginx
```

## 3. Build Spring Boot application and copy it to ec2 via ssh

I use maven in my project, so I'll build it using this command:
```
mvn clean install
```
The target folder with jar file should be created. Now we can send the jar to the server using this command:
```
scp gallery-webapp-0.0.1-SNAPSHOT.jar ec2-user@54.221.64.30:~/
```
Jar file appears in the user folder. I'll create the separate folder for the app and move the jar there:
```
mkdir app
mv gallery-webapp-0.0.1-SNAPSHOT.jar ./app
```

## 4. Configure startup script and environment variables

My application uses environment variables to store settings. I found it easier to create the run script and export the variables in it.

Navigate to app directory, create a script to run your application and assign the permissions.
```
cd app
touch run.sh
chmod -x run.sh
```
Now open the file (I use `nano`), add shebang, path variables and the command to run your application.
```
#!/bin/bash

export GALLERY_DATASOURCE_URL='<data>'
export GALLERY_DATASOURCE_PASSWORD='<data>'
export GALLERY_DATASOURCE_USER='<data>'
export MAIN_S3_BUCKET='<data>'
export MAIN_URL='<data>'
export S3_ACCESS_KEY='<data>'
export S3_SECRET_KEY='<data>'
export SECRET_KEY='<data>'

java -jar gallery-webapp-0.0.1-SNAPSHOT.jar
```

Now you can run the app using following command:
```
sudo ./run.sh
```

## 5. Adjust nginx settings

Now we need to adjust nginx to proxy inbound requests to the application.

Navigate to `/etc/nginx/`
```
cd /etc/nginx
```

Create a new file in `/etc/nginx/available-sites/` folder
```
sudo touch ./available-sites/gallery
```

Open the created file and add following configs to it:
```
server {
    listen       80;
    listen       [::]:80;
    server_name gallery www.gallery;
    
    location / {
            proxy_pass http://localhost:8080;
            proxy_set_header  X-Real-IP $remote_addr;
            proxy_set_header  X-Forwarded-For$proxy_add_x_forwarded_for;
            proxy_set_header  Host $http_host;
    }
}
```
This config redirects requests to your app.

Now we need to enable the configuration.
Link the config file to `etc/nginx/sites-enabled`
```
sudo ln -s /etc/nginx/sites-available/gallery /etc/nginx/sites-enabled/
```

The default nginx configuration overrides the behavior of our configuration, so lets remove it.
```
sudo rm /etc/nginx/sites-enabled/default
```

Now restart the nginx process
```
sudo systemctl restart nginx
```

## 6. Adjust systemd and run the app

In order to run your application as a service you need to add systemd configuration. Navigate to `/etc/systemd/system` and create the configuration file.
```
cd /etc/systemd/system
sudo touch gallery.service
```

Now open the file and add following configs:
```
[Unit]
Description=Gallery Web Application

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app/
ExecStart=/home/ubuntu/app/run.sh
Restart=always
```

Now restart systemd daemon and run the application:
```
sudo systemctl daemon-reexec
sudo systemctl start gallery.service
```

You can check whether you app is running using this command
```
sudo systemctl status gallery.service
```

If the application was started successfully you can now access your application using the instance public ip address from the EC2 in the browser.
