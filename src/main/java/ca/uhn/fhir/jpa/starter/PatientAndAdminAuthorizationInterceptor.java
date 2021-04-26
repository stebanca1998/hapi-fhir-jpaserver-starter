package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

import java.util.List;
import org.hl7.fhir.r4.model.IdType;


@SuppressWarnings("ConstantConditions")
public class PatientAndAdminAuthorizationInterceptor extends AuthorizationInterceptor {
   
   @Override
   public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
      
      // Process authorization header - The following is a fake 
      // implementation. Obviously we'd want something more real
      // for a production scenario.
      // 
      // In this basic example we have two hardcoded bearer tokens,
      // one which is for a user that has access to one patient, and
      // another that has full access. 
      IdType userIdPatientId = null;
      boolean userIsAdmin = false;
      String authHeader = theRequestDetails.getHeader("Authorization");

      /*
      if (authHeader == null || authHeader.startsWith("Basic ") == false) {
         throw new AuthenticationException("Missing or invalid Authorization header");
      }

      String base64 = authHeader.substring("Basic ".length());
      String base64decoded = new String(Base64.decodeBase64(base64));
      String[] parts = base64decoded.split(":");

      String username = parts[0];
      String password = parts[1];*/

      if ("Bearer dfw98h38r".equals(authHeader)) {
         // This user has access only to Patient/1 resources
         userIdPatientId = new IdType("Patient", 4L);
      } else if ("Bearer 39ff939jgg".equals(authHeader)) {
         // This user has access to everything
         userIsAdmin = true;
      } else {
         // Throw an HTTP 401
         throw new AuthenticationException("Missing or invalid Authorization header value");
      }

      // If the user is a specific patient, we create the following rule chain:
      // Allow the user to read anything in their own patient compartment
      // Allow the user to write anything in their own patient compartment
      // If a client request doesn't pass either of the above, deny it
      if (userIdPatientId != null) {
         return new RuleBuilder()
            .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
            .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
            .denyAll()
            .build();
      }
      
      // If the user is an admin, allow everything
      if (userIsAdmin) {
         return new RuleBuilder()
            .allowAll()
            .build();
      }
      
      // By default, deny everything. This should never get hit, but it's 
      // good to be defensive
      return new RuleBuilder()
         .denyAll()
         .build();
   }
}
