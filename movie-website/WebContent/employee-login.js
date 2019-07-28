function handleLoginResult(resultDataJson) {
	resultDataJson = JSON.parse(resultDataJson);
	if (resultDataJson["status"] === "success") {
        window.location.replace("dashboard.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson);
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(event) {
    event.preventDefault();
    $.post(
    		"api/employee-login",
            // Serialize the login form to the data sent by POST request
            $("#login_form").serialize(),
            (resultDataString) => handleLoginResult(resultDataString)
        );
}


$("#login_form").submit((event) => submitLoginForm(event));
