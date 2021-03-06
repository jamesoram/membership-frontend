```
                            __                            __
 /'\_/`\                   /\ \                          /\ \      __
/\      \     __    ___ ___\ \ \____     __   _ __   ____\ \ \___ /\_\  _____
\ \ \__\ \  /'__`\/' __` __`\ \ '__`\  /'__`\/\`'__\/',__\\ \  _ `\/\ \/\ '__`\
 \ \ \_/\ \/\  __//\ \/\ \/\ \ \ \L\ \/\  __/\ \ \//\__, `\\ \ \ \ \ \ \ \ \L\ \
  \ \_\\ \_\ \____\ \_\ \_\ \_\ \_,__/\ \____\\ \_\\/\____/ \ \_\ \_\ \_\ \ ,__/
   \/_/ \/_/\/____/\/_/\/_/\/_/\/___/  \/____/ \/_/ \/___/   \/_/\/_/\/_/\ \ \/
                                                                          \ \_\
                                                                           \/_/
```

# Membership Frontend

## Table of Contents

1. [Getting Started](#getting-started)
2. [Setup](#setup)
3. [Run](#run)
4. [Tests](#tests)
5. [Client-side Builds](#cs-builds)
6. [Development Principles](#dev-principles)
7. [Pattern Library](#pattern-library)
8. [Building AMIs](#building-amis)
9. [Test Users](#test-users)
10. [Security](#security)
11. [Troubleshooting](#troubleshooting)

<a name="getting-started">
## Getting Started

To get started working on Membership you will need to complete the following steps:

1. Work through the **General Setup** instructions for this project
2. Work through the setup instructions for [Identity](https://github.com/guardian/identity) and [theguardian.com](https://github.com/guardian/identity)
3. Start up Membership by running the commands in the [Run](#run) section of this README

### Client-side feature development

If you are doing client-side work on Membership you should also make yourself familar with the following:

- Look over the [pattern library](https://membership.theguardian.com/patterns) to get an understanding of the design language of the site.
- Read through [FRONTEND.md](docs/FRONTEND.md) to get a high-level understanding of how our client-side code is structured.

<a name="setup">
## General Setup

1. Go to project root
1. If you don't have `bower`, `grunt`, or `sass`, run `./setup-tools.sh` to install them globally on your system.
1. Run `./setup.sh` to install project-specific client-side dependencies.
1. Add the following to your `/etc/hosts`

   ```
   127.0.0.1   mem.thegulocal.com
   ```

1. Run `./nginx/setup.sh`
1. Download our private keys from the `membership-private` S3 bucket. You will need an AWS account so ask another dev.

    If you have the AWS CLI set up you can run
    ```
    aws s3 cp s3://membership-private/DEV/membership-keys.conf /etc/gu
    ```
1. In ~/.bash_profile add:

```
export AWS_ACCESS_KEY=<access-key-id>
export AWS_SECRET_KEY=<secret-key>
```

### Ubuntu

In an ideal world, your Ubuntu package install would be:

```
$ sudo apt-get install nginx openjdk-7-jdk ruby ruby-dev nodejs npm
```

#### [Node](http://nodejs.org/) & [NPM](https://github.com/npm/npm/releases)

See Joyent's instructions on [installing Node & NPM on Ubuntu](https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager#ubuntu-mint-elementary-os).
If the available [nodejs](http://packages.ubuntu.com/trusty/nodejs) or [NPM](http://packages.ubuntu.com/trusty/npm)
package for your version of Ubuntu is old, you'll [probably](http://askubuntu.com/questions/49390/how-do-i-install-the-latest-version-of-node-js)
want to install [chris-lea's PPA](https://launchpad.net/~chris-lea/+archive/node.js),
which includes both Node.js and NPM.

<a name="run">
## Run

The app normally runs on port `9100`. You can run the following commands to start the app (separate console windows)

```
 nginx/setup.sh
./start-frontend.sh
```

### Run membership locally with Identity on theguardian.com

To be able to go through the registration flow locally you will need to have the Identity API and Identity project in theguardian.com running.

**Identity repo**: [https://github.com/guardian/identity](https://github.com/guardian/identity)

Run through the set up instructions; once complete you will need to run:

```
 nginx/setup.sh
 ./start-api.sh
```

**theguardian.com frontend repo**: [https://github.com/guardian/frontend](https://github.com/guardian/frontend)

Run through the set up instructions - note you need to make sure that your Frontend is set up to point at your _local_ Identity, not the `CODE` Identity, which means adding this to your `frontend.properties`:

```
id.apiRoot=https://idapi.thegulocal.com
id.apiClientToken=frontend-dev-client-token
```

Once complete you will need to run:

```
nginx/setup.sh
./sbt
project identity
idrun
```

<a name="tests">
## Tests

<a name="tests-scala">
### Running Scala unit tests

To run Scala unit tests run `sbt test` from the root of the project.

<a name="tests-js">
### Running JavaScript unit tests

**Note these commands should be run from inside the `frontend/` directory**

```
grunt karma --dev
```

#### Test coverage

Run with `--dev` flag to generate test coverage. Coverage report can be found in `frontend/test/js/coverage/`.

<a name="tests-functional">
### Running functional tests

See [README.md](functional-tests/README.md) for details on how to run Membership functional tests.

<a name="cs-builds">
## Client-side Builds

Client-side build are handled using Grunt. **Note these commands should be run from inside the `frontend/` directory**

### Watch and compile front-end files

```
grunt watch --dev
```

### Compile front-end files

```
grunt compile --dev
```

<a name="dev-principles">
## Development Principles

### Client-side Principles

See [FRONTEND.md](FRONTEND.md) for high-level client-side principles for Membership.

<a name="pattern-library">
## Pattern Library

A library of common patterns used accross the membership site is available at [membership.theguardian.com/patterns](https://membership.theguardian.com/patterns)

When building new components break them down into fragments and include them in the pattern library.

<a name="building-amis">
### Building AMIs

We use [packer](http://www.packer.io) to create new AMIs, you can download it here: http://www.packer.io/downloads.html.
If you are on OS X and using Homebrew, you can install Packer by adding the `binary` tap:
```
brew tap homebrew/binary
brew install packer
```


To create an AMI, you must set `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` as described above.

To add your requirements to the new AMI, you should update `provisioning.json`. This will probably involve editing the `provisioners` section, but more information can be found in the [packer docs](http://www.packer.io/docs).

To get the latest list of AMIs you can use the following AWS command:
```
aws ec2 describe-images  --owner 099720109477 --region eu-west-1 --filter "Name=architecture,Values=x86_64" --filter "Name=virtualization-type,Values=hvm" | jq '.Images[] | select(contains({RootDeviceType: "ebs"})) | .ImageId + " " + .Name' | grep hvm-ssd/ubuntu-trusty-14.04-amd64-server | sort -r --key 2 | head -6
```

(Note the example above uses ```ubuntu-trusty-14.04-amd64-server``` but feel free to use a more up to releases.)

Once you are ready, run the following:
```
packer build provisioning.json
```
This will take several minutes to build the new AMI. Once complete, you should see something like:
```
eu-west-1: ami-xxxxxxxx
```

Use the Deployment steps below to test your new AMI

1. Turn off continuous deployment in RiffRaff
1. Update the CloudFormation parameter `ImageId` <b>(make a note of the current value first)</b>
1. Increase the autoscale group size by 1
1. Test the new box
1. If it doesn't work, revert the value of `ImageId`
1. Run a full deploy in RiffRaff
1. Decrease autoscale group size by 1
1. Re-enable continous deployment

<a name="test-users">
## Test Users

See https://sites.google.com/a/guardian.co.uk/guardan-identity/identity/test-users for details of how we do test users.
Note that we read the shared secret for these from the `identity.test.users.secret` property in `membership-keys.conf`.

<a name="security">
## Security

### Committing config credentials

For the Membership project, we put both `DEV` and `PROD` credentials in `membership-keys.conf` files in the private S3 bucket `membership-private`, and if private credentials need adding or updating, they need to be updated there in S3.

You can download and update credentials like this

    aws s3 cp s3://membership-private/DEV/membership-keys.conf /etc/gu
    aws s3 cp /etc/gu/membership-keys.conf s3://membership-private/DEV/

For a reminder on why we do this, here's @tackley on the subject:

>NEVER commit access keys, passwords or other sensitive credentials to any source code repository*.

>That's especially true for public repos, but also applies to any private repos. (Why? 1. it's easy to make it public and 2. every person who ever worked on your project almost certainly has a copy of your repo somewhere. It's too easy for a subsequently-disaffected individual to take advantage of this.)

>For AWS access keys, always prefer to use instance profiles instead.

>For other credentials, either use websys's puppet based config distribution (for websys managed machines) or put them in a configuration store such as DynamoDB or a private S3 bucket.

<a name="troubleshooting">
## Troubleshooting

See [Troubleshooting.md](docs/Troubleshooting.md) for information on common problems and how to fix them.
