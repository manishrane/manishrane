package com.example.demo;

import org.apache.hadoop.conf.Configuration;

public class DeltaLakeConfiguration {

    public static Configuration getConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", Constants.ABSS_FILE_PATH);
        conf.set("fs.azure.account.key.ubsgen2storageuk.dfs.core.windows.net", Constants.ACCESS_KEY);
        return conf;
    }

}
