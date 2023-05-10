package ru.isu.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FilesTest {

    @Test
    public void hasCode() {
        String str = "1.zip";
        assertTrue(str.matches("\\d*.zip"));

        String s2 = "dodod.zip";
        assertTrue(s2.substring(0, s2.indexOf(".")).equals("dodod"));
    }

    @Test
    public void textSplit() {
        String s = "failed-assert /Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}patientRole[1]/Q{urn:hl7-org:v3}providerOrganization[1] Core06-1. Элемент //identity:Props должен иметь 1 элемент identity:Ogrnip.";
        int end1 = s.indexOf(" ");
        String s1 = s.substring(0, end1);
        assertEquals(s1, "failed-assert");
        s = s.substring(end1+1);
        end1 = s.indexOf(" ");
        assertEquals(s.substring(0, end1), "/Q{urn:hl7-org:v3}ClinicalDocument[1]/Q{urn:hl7-org:v3}recordTarget[1]/Q{urn:hl7-org:v3}patientRole[1]/Q{urn:hl7-org:v3}providerOrganization[1]");
        assertEquals(s.substring(end1+1), "Core06-1. Элемент //identity:Props должен иметь 1 элемент identity:Ogrnip.");
    }



}