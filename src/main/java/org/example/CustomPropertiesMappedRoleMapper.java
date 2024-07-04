package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.wildfly.security.authz.RoleMapper;
import org.wildfly.security.authz.Roles;

public class CustomPropertiesMappedRoleMapper implements RoleMapper {

    Map<String,String> configuration;

    final String ROLE_PROPERTIES = "ROLE_PROPERTIES";

    public void initialize(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    @Override
    public Roles mapRoles(Roles rolesToMap) {

        Set<String> rolesToAdd = new java.util.HashSet<String>();

        Set<Map.Entry<String, String>> propertiesMap = getPropertiesMap(configuration.get(ROLE_PROPERTIES));

        for(Map.Entry<String,String> entry: propertiesMap) {
            if (rolesToMap.contains(entry.getKey())) {
                for(String role: entry.getValue().split(",")) {
                    rolesToAdd.add(role);
                }
            }
        }

        if (rolesToAdd.size() == 0) {
            return rolesToMap;
        }

        return rolesToMap.or(Roles.fromSet(rolesToAdd));
    }
    
    private Set<Map.Entry<String, String>> getPropertiesMap(String roleProperties) {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(roleProperties);
            prop.load(input);

            Set<Map.Entry<String, String>> entries = prop.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            e -> (String) e.getValue()))
                    .entrySet();

            return entries;

        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
