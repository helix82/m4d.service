package su.opencode.digsigcik.m4d.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
//    private String token_type;
//    private String refresh_token;
//    private int expires_in;
//    private String scope;
//    private String accountId;
//    private CompanyInfo companyInfo;
//
//    public class CompanyInfo {
//        private String id;
//        private String nodeAlias;
//        private List<Node> nodes;
//
//        public class Node {
//            private String alias;
//            private String ownerAddress;
//        }
//
//        private String registrationMethod;
//        private String type;
//        private String name;
//        private String shortName;
//        private String address;
//        private String phone;
//        private String email;
//        private String okpo;
//        private String ogrn;
//        private String inn;
//        private String kpp;
//        private Meta meta;
//
//        public class Meta {
//            private List<App> apps;
//            private String operatorId;
//
//            public class App {
//                private String code;
//                private String name;
//                private List<String> businessRoles;
//            }
//        }
//    }
//    private PersonInfo personInfo;
//
//    public class PersonInfo {
//        private String personId;
//        private String accountId;
//        private String firstName;
//        private String lastName;
//        private String patronymic;
//        private String email;
//        private String phone;
//        private String participantAddress;
//        private String participantPublicKey;
//        private String companyId;
//        private String companyName;
//        private String companyRegistrationMethod;
//        private String nodeAlias;
//        private List<String> nodeAliases;
//        private List<String> businessRoles;
//        private String displayName;
//        private CompanyInfo.Meta meta;
//
//        public class Meta {
//            private List<CompanyInfo.Meta.App> apps;
//            private String operatorId;
//
//            public class App {
//                private String code;
//                private String name;
//                private List<String> businessRoles;
//            }
//        }
//    }
//    private String jti;
}
