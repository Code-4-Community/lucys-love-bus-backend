import com.codeforcommunity.dto.auth.NewUserAsPFRequest;

public interface IPfOperationsProcessor {

	//TODO java doc

	// signs a user up and makes a request for them to be PF. Will allow user to 
	// go about there business as a GP until it is accepted ... 
	SessionResponse signUpPF(NewUserAsPFRequest newUserAsPFRequest);

}