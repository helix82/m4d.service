package su.opencode.digsigcik.synapi.revokedCertificate;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import su.opencode.digsigcik.synapi.dto.RevokedCertificateDTO;
import su.opencode.digsigcik.synapi.model.RevokedCertificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RevokedCertificateUnitTest {
    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    public void whenConvertEntityToDto_thenCorrect() {
        RevokedCertificate revokedCertificate = new RevokedCertificate();
        revokedCertificate.setId(1L);
        revokedCertificate.setImprint("9BA648660733ED7A550BCEA03A20E14B8F25C99B");
        revokedCertificate.setKeyId("29B40D88F9A81CC544BF50DE656903955B0523D1");
        revokedCertificate.setIssuedTo("Минкомсвязь России");
        revokedCertificate.setDateStart("2018-01-26T09:15:35Z");
        revokedCertificate.setDateEnd("2027-01-26T09:15:35Z");
        revokedCertificate.setCertificate("MIIFGTCCBMigAwIBAgIQNGgeQMtB7zOpoLfIdpKaKTAIBgYqhQMCAgMwggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMB4XDTEyMDcyMDEyMzExNFoXDTI3MDcxNzEyMzExNFowggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMGMwHAYGKoUDAgITMBIGByqFAwICIwEGByqFAwICHgEDQwAEQI+lv3kQI8jWka1kMVdbvpvFioP0Pyn3Knmp+2XD6KgPWnXEIlSRX8g/IYracDr51YsNc2KE3C7mkH6hA3M3ofujggGCMIIBfjCBxgYFKoUDZHAEgbwwgbkMI9Cf0JDQmtCcIMKr0JrRgNC40L/RgtC+0J/RgNC+IEhTTcK7DCDQn9CQ0JogwqvQk9C+0LvQvtCy0L3QvtC5INCj0KbCuww20JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS8zLzIvMi05OTkg0L7RgiAwNS4wNy4yMDEyDDjQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzcvMS80LzItNjAzINC+0YIgMDYuMDcuMjAxMjAuBgUqhQNkbwQlDCPQn9CQ0JrQnCDCq9Ca0YDQuNC/0YLQvtCf0YDQviBIU03CuzBDBgNVHSAEPDA6MAgGBiqFA2RxATAIBgYqhQNkcQIwCAYGKoUDZHEDMAgGBiqFA2RxBDAIBgYqhQNkcQUwBgYEVR0gADAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUi5g7iRhR6O+cAni46sjUILJVyV0wCAYGKoUDAgIDA0EA23Reec/Y27rpMi+iFbgWCazGY3skBTq5ZGsQKOUxCe4mO7UBDACiWqdA0nvqiQMXeHgqo//fO9pxuIHtymwyMg==");

        RevokedCertificateDTO revokedCertificateDTO = modelMapper.map(revokedCertificate, RevokedCertificateDTO.class);
        assertEquals(revokedCertificate.getId(), revokedCertificateDTO.getId());
        assertEquals(revokedCertificate.getImprint(), revokedCertificateDTO.getImprint());
        assertEquals(revokedCertificate.getKeyId(), revokedCertificateDTO.getKeyId());
        assertEquals(revokedCertificate.getIssuedTo(), revokedCertificateDTO.getIssuedTo());
        assertEquals(revokedCertificate.getDateStart(), revokedCertificateDTO.getDateStart());
        assertEquals(revokedCertificate.getDateEnd(), revokedCertificateDTO.getDateEnd());
        assertEquals(revokedCertificate.getCertificate(), revokedCertificateDTO.getCertificate());
    }

    @Test
    public void whenConvertDtoToEntity_thenCorrect() {
        RevokedCertificateDTO revokedCertificateDTO = new RevokedCertificateDTO();
        revokedCertificateDTO.setId(1L);
        revokedCertificateDTO.setImprint("9BA648660733ED7A550BCEA03A20E14B8F25C99B");
        revokedCertificateDTO.setSerialNumber("00A0D0986100000000022A");
        revokedCertificateDTO.setKeyId("29B40D88F9A81CC544BF50DE656903955B0523D1");
        revokedCertificateDTO.setIssuedTo("Минкомсвязь России");
        revokedCertificateDTO.setDateStart("2018-01-26T09:15:35Z");
        revokedCertificateDTO.setDateEnd("2027-01-26T09:15:35Z");
        revokedCertificateDTO.setCertificate("MIIFGTCCBMigAwIBAgIQNGgeQMtB7zOpoLfIdpKaKTAIBgYqhQMCAgMwggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMB4XDTEyMDcyMDEyMzExNFoXDTI3MDcxNzEyMzExNFowggFKMR4wHAYJKoZIhvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRUwEwYDVQQHDAzQnNC+0YHQutCy0LAxPzA9BgNVBAkMNjEyNTM3NSDQsy4g0JzQvtGB0LrQstCwLCDRg9C7LiDQotCy0LXRgNGB0LrQsNGPLCDQtC4gNzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40LgxGDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0NzQzNzUxQTA/BgNVBAMMONCT0L7Qu9C+0LLQvdC+0Lkg0YPQtNC+0YHRgtC+0LLQtdGA0Y/RjtGJ0LjQuSDRhtC10L3RgtGAMGMwHAYGKoUDAgITMBIGByqFAwICIwEGByqFAwICHgEDQwAEQI+lv3kQI8jWka1kMVdbvpvFioP0Pyn3Knmp+2XD6KgPWnXEIlSRX8g/IYracDr51YsNc2KE3C7mkH6hA3M3ofujggGCMIIBfjCBxgYFKoUDZHAEgbwwgbkMI9Cf0JDQmtCcIMKr0JrRgNC40L/RgtC+0J/RgNC+IEhTTcK7DCDQn9CQ0JogwqvQk9C+0LvQvtCy0L3QvtC5INCj0KbCuww20JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS8zLzIvMi05OTkg0L7RgiAwNS4wNy4yMDEyDDjQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzcvMS80LzItNjAzINC+0YIgMDYuMDcuMjAxMjAuBgUqhQNkbwQlDCPQn9CQ0JrQnCDCq9Ca0YDQuNC/0YLQvtCf0YDQviBIU03CuzBDBgNVHSAEPDA6MAgGBiqFA2RxATAIBgYqhQNkcQIwCAYGKoUDZHEDMAgGBiqFA2RxBDAIBgYqhQNkcQUwBgYEVR0gADAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUi5g7iRhR6O+cAni46sjUILJVyV0wCAYGKoUDAgIDA0EA23Reec/Y27rpMi+iFbgWCazGY3skBTq5ZGsQKOUxCe4mO7UBDACiWqdA0nvqiQMXeHgqo//fO9pxuIHtymwyMg==");
        revokedCertificateDTO.setTypeInput("Вручную");
        revokedCertificateDTO.setAccreditationStatus("VALID");

        RevokedCertificate revokedCertificate = modelMapper.map(revokedCertificateDTO, RevokedCertificate.class);
        assertEquals(revokedCertificateDTO.getId(), revokedCertificate.getId());
        assertEquals(revokedCertificateDTO.getImprint(), revokedCertificate.getImprint());
        assertEquals(revokedCertificateDTO.getKeyId(), revokedCertificate.getKeyId());
        assertEquals(revokedCertificateDTO.getIssuedTo(), revokedCertificate.getIssuedTo());
        assertEquals(revokedCertificateDTO.getDateStart(), revokedCertificate.getDateStart());
        assertEquals(revokedCertificateDTO.getDateEnd(), revokedCertificate.getDateEnd());
        assertEquals(revokedCertificateDTO.getCertificate(), revokedCertificate.getCertificate());
    }
}
