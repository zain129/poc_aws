package com.ast.poc_aws.controller;

import com.ast.poc_aws.model.PersonData;
import com.ast.poc_aws.service.PersonDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

//@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
@Slf4j
public class PersonDataController {

    @Autowired
    private PersonDataService personDataService;

    static final String URI = "http://localhost:8081/api";

    @GetMapping("/getAndValidate")
    public ResponseEntity getAndValidate() {
        try {
            PersonData personDataFromJSON;
            String jsonDoc = personDataService.getJsonDoc();
            personDataFromJSON = personDataService.getPersonDataFromJSON(jsonDoc);
            if (personDataService.validateData(personDataFromJSON.getDateOfBirth(), personDataFromJSON.getWeight())) {
                PersonData personData = personDataService.insertDirectIntoPG(personDataFromJSON);
                int id = personDataService.insertUsingSpIntoPG(personDataFromJSON);

                /* Calling another service */
                log.info("Calling another API '/2/printData'");
                final String uri = URI + "/2/printData";
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> result = restTemplate.postForEntity(uri, personData, String.class);
                return result;
            } else {
                log.info("Calling another API '/2/sayError'");
                final String uri = URI + "/2/sayError?name=" + personDataService.getNameOnly(jsonDoc);

                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.getForObject(uri, String.class);
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception ex) {
            log.error("Exception in Controller -> getAndValidate: ", ex);
        }
        return ResponseEntity.badRequest().body("Invalid Data");
    }
}
