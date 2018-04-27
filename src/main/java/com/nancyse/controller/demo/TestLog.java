package com.nancyse.controller.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class TestLog {
	public static void main(String[] args) throws IOException {
		Logger logger = LogManager.getLogger("log1"); // LoggerµÄÃû³Æ

        logger.trace("trace level: nancyse");
        logger.debug("debug level");
        logger.info("info level");
        logger.warn("warn level: nancyse");
        logger.error("error level");
        logger.fatal("fatal level");
	}

}
