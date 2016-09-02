package com.kameo.challenger;

import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.CucumberOptions;
import cucumber.api.java.Before;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:cucumber")
public class CucumberTest {



}