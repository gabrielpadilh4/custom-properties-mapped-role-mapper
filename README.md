# Elytron custom Mapped Role Mapper using a properties file

## Overview

This project provides a custom mapped role mapper that can use a properties file in order to "transform" Realm roles to Application roles.

The use case for this mapper is take groups from the JBoss / Wildfly realm into Application roles by using a properties file.

The same use case of Mapped Role Mapper, however, a properties is used as a source.

This was tested with JBoss EAP 8 and Wildfly 28.

## Installation

To use this custom role mapper in your JBoss / Wildfly instance, follow these steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/gabrielpadilh4/custom-properties-mapped-role-mapper.git
   ```

2. **Compile the project**
   ```bash
   mvn clean install
   ```
3. **Install the artifact as a module in JBoss / Wildfly**
   With the server stopped, add the module:
   ```bash
   $JBOSS_HOME/jboss-cli.sh
   module add --name=org.example.custom-properties-mapped-role-mapper --resources=<PROJECT_DIRECTORY>/target/custom-properties-mapped-role-mapper-1.0.0.jar --dependencies=org.wildfly.security.elytron
   ```
4. **Configure the custom role mapper**
   ```bash
   /subsystem=elytron/custom-role-mapper=CustomPropertiesMappedRoleMapper:add(class-name=org.example.CustomPropertiesMappedRoleMapper, module=org.example.custom-properties-mapped-role-mapper,configuration={ROLE_PROPERTIES=>/jboss/standalone/configuration/rolesMapping-roles.properties})
   ```
   Replace the properties file with the full path of your own properties.

5. **Use the custom role mapper in the security domain**
   ```bash
   /subsystem=elytron/security-domain=YOUR-APPLICATION-DOMAIN:write-attribute(name=role-mapper,value=CustomPropertiesMappedRoleMapper)
   ```
## Use case

I have a security domain configured to use LDAP as authentication, LDAP also provides the groups for my users, however, i need to transform the LDAP roles into Application roles since it has a different name.

A user `jbrown` is assigned to groups `ldap-user,ldap-admin and EA`. Users assigned with role `EA` in LDAP, should have the roles `NavigatorUser, AdminUser` in my application.

The content of my `rolesMapping-roles.properties` has the following:
~~~
#LDAP GROUP,APP ROLES
EA=NavigatorUser,AdminUser
~~~

In JBoss / Wildfly, the following line represents the end result of the custom role mapper:
```
2024-07-03 22:46:43,536 TRACE [org.wildfly.security] (default task-1) Role mapping: principal [jbrown] -> decoded roles [ldap-user, ldap-admin, EA] -> domain decoded roles [] -> realm mapped roles [ldap-user, ldap-admin, EA] -> domain mapped roles [ldap-user, ldap-admin, EA, NavigatorUser, AdminUser]
```

As we can see, the realm mapped roles are `ldap-user, ldap-admin, EA` that once passes by the custom role mapper turns into `ldap-user, ldap-admin, EA, NavigatorUser, AdminUser`.

The roles `NavigatorUser, AdminUser` have been added since the user is part of group EA in LDAP.