package su.opencode.digsigcik.synapi.trustedCertificate;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import su.opencode.digsigcik.synapi.dto.TrustedCertificateDTO;
import su.opencode.digsigcik.synapi.model.TrustedCertificate;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TrustedCertificateUnitTest {
    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    public void whenConvertEntityToDto_thenCorrect() {
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId(1L);
        trustedCertificate.setImprint("9BA648660733ED7A550BCEA03A20E14B8F25C99B");
        trustedCertificate.setSerialNumber("00A0D0986100000000022A");
        trustedCertificate.setKeyId("29B40D88F9A81CC544BF50DE656903955B0523D1");
        trustedCertificate.setIssuedTo("Минкомсвязь России");
        trustedCertificate.setDateStart("2018-01-26T09:15:35Z");
        trustedCertificate.setDateEnd("2027-01-26T09:15:35Z");
        trustedCertificate.setCertificate("MIIFGTCCBMigAwIBAgIQNGgeQMtB7zOpoLfIdpKaKTAIBgYqhQMCAgMwggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMB4XDTEyMDcyMDEyMzExNFoXDTI3MDcxNzEyMzExNFowggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMGMwHAYGKoUDAgITMBIGByqFAwICIwEGByqFAwICHgEDQwAEQI+lv3kQI8jWka1kMVdbvpvFioP0Pyn3Knmp+2XD6KgPWnXEIlSRX8g/IYracDr51YsNc2KE3C7mkH6hA3M3ofujggGCMIIBfjCBxgYFKoUDZHAEgbwwgbkMI9Cf0JDQmtCcIMKr0JrRgNC40L/RgtC+0J/RgNC+IEhTTcK7DCDQn9CQ0JogwqvQk9C+0LvQvtCy0L3QvtC5INCj0KbCuww20JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS8zLzIvMi05OTkg0L7RgiAwNS4wNy4yMDEyDDjQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzcvMS80LzItNjAzINC+0YIgMDYuMDcuMjAxMjAuBgUqhQNkbwQlDCPQn9CQ0JrQnCDCq9Ca0YDQuNC/0YLQvtCf0YDQviBIU03CuzBDBgNVHSAEPDA6MAgGBiqFA2RxATAIBgYqhQNkcQIwCAYGKoUDZHEDMAgGBiqFA2RxBDAIBgYqhQNkcQUwBgYEVR0gADAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUi5g7iRhR6O+cAni46sjUILJVyV0wCAYGKoUDAgIDA0EA23Reec/Y27rpMi+iFbgWCazGY3skBTq5ZGsQKOUxCe4mO7UBDACiWqdA0nvqiQMXeHgqo//fO9pxuIHtymwyMg==");
        trustedCertificate.setTypeInput("Вручную");
        trustedCertificate.setAccreditationStatus("VALID");

        TrustedCertificateDTO trustedCertificateDTO = modelMapper.map(trustedCertificate, TrustedCertificateDTO.class);
        assertEquals(trustedCertificate.getId(), trustedCertificateDTO.getId());
        assertEquals(trustedCertificate.getImprint(), trustedCertificateDTO.getImprint());
        assertEquals(trustedCertificate.getSerialNumber(), trustedCertificateDTO.getSerialNumber());
        assertEquals(trustedCertificate.getKeyId(), trustedCertificateDTO.getKeyId());
        assertEquals(trustedCertificate.getIssuedTo(), trustedCertificateDTO.getIssuedTo());
        assertEquals(trustedCertificate.getDateStart(), trustedCertificateDTO.getDateStart());
        assertEquals(trustedCertificate.getDateEnd(), trustedCertificateDTO.getDateEnd());
        assertEquals(trustedCertificate.getCertificate(), trustedCertificateDTO.getCertificate());
        assertEquals(trustedCertificate.getTypeInput(), trustedCertificateDTO.getTypeInput());
        assertEquals(trustedCertificate.getAccreditationStatus(), trustedCertificateDTO.getAccreditationStatus());
    }

    @Test
    public void whenConvertDtoToEntity_thenCorrect() {
        TrustedCertificateDTO trustedCertificateDTO = new TrustedCertificateDTO();
        trustedCertificateDTO.setId(1L);
        trustedCertificateDTO.setImprint("9BA648660733ED7A550BCEA03A20E14B8F25C99B");
        trustedCertificateDTO.setSerialNumber("00A0D0986100000000022A");
        trustedCertificateDTO.setKeyId("29B40D88F9A81CC544BF50DE656903955B0523D1");
        trustedCertificateDTO.setIssuedTo("Минкомсвязь России");
        trustedCertificateDTO.setDateStart("2018-01-26T09:15:35Z");
        trustedCertificateDTO.setDateEnd("2027-01-26T09:15:35Z");
        trustedCertificateDTO.setCertificate("MIIFGTCCBMigAwIBAgIQNGgeQMtB7zOpoLfIdpKaKTAIBgYqhQMCAgMwggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMB4XDTEyMDcyMDEyMzExNFoXDTI3MDcxNzEyMzExNFowggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMGMwHAYGKoUDAgITMBIGByqFAwICIwEGByqFAwICHgEDQwAEQI+lv3kQI8jWka1kMVdbvpvFioP0Pyn3Knmp+2XD6KgPWnXEIlSRX8g/IYracDr51YsNc2KE3C7mkH6hA3M3ofujggGCMIIBfjCBxgYFKoUDZHAEgbwwgbkMI9Cf0JDQmtCcIMKr0JrRgNC40L/RgtC+0J/RgNC+IEhTTcK7DCDQn9CQ0JogwqvQk9C+0LvQvtCy0L3QvtC5INCj0KbCuww20JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS8zLzIvMi05OTkg0L7RgiAwNS4wNy4yMDEyDDjQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzcvMS80LzItNjAzINC+0YIgMDYuMDcuMjAxMjAuBgUqhQNkbwQlDCPQn9CQ0JrQnCDCq9Ca0YDQuNC/0YLQvtCf0YDQviBIU03CuzBDBgNVHSAEPDA6MAgGBiqFA2RxATAIBgYqhQNkcQIwCAYGKoUDZHEDMAgGBiqFA2RxBDAIBgYqhQNkcQUwBgYEVR0gADAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUi5g7iRhR6O+cAni46sjUILJVyV0wCAYGKoUDAgIDA0EA23Reec/Y27rpMi+iFbgWCazGY3skBTq5ZGsQKOUxCe4mO7UBDACiWqdA0nvqiQMXeHgqo//fO9pxuIHtymwyMg==");
        trustedCertificateDTO.setTypeInput("Вручную");
        trustedCertificateDTO.setAccreditationStatus("VALID");

        TrustedCertificate trustedCertificate = modelMapper.map(trustedCertificateDTO, TrustedCertificate.class);
        assertEquals(trustedCertificateDTO.getId(), trustedCertificate.getId());
        assertEquals(trustedCertificateDTO.getImprint(), trustedCertificate.getImprint());
        assertEquals(trustedCertificateDTO.getSerialNumber(), trustedCertificate.getSerialNumber());
        assertEquals(trustedCertificateDTO.getKeyId(), trustedCertificate.getKeyId());
        assertEquals(trustedCertificateDTO.getIssuedTo(), trustedCertificate.getIssuedTo());
        assertEquals(trustedCertificateDTO.getDateStart(), trustedCertificate.getDateStart());
        assertEquals(trustedCertificateDTO.getDateEnd(), trustedCertificate.getDateEnd());
        assertEquals(trustedCertificateDTO.getCertificate(), trustedCertificate.getCertificate());
        assertEquals(trustedCertificateDTO.getTypeInput(), trustedCertificate.getTypeInput());
        assertEquals(trustedCertificateDTO.getAccreditationStatus(), trustedCertificate.getAccreditationStatus());
    }
}
