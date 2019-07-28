function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/*
 * This function is only useful when the user decides to
 * update a quantity of a movie to 0. If this happens,
 * a JSON object containing the updated movies will be used 
 * to update the cart page.
 */
function handleUpdateDisplayData(json){
	if ( json["servlet_response_no_refresh"]) {
		return;
	}
	else{
		displayCart(json);
	}
}


/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
	console.log(resultDataString);
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

}


/*
 * This function is called when user updates a quantity of Movie 
 */
function handleUpdateCart (event){
	event.preventDefault();
	jQuery.ajax({
		dataType: "json",
	    method: "GET", 
	    url: "api/cart?update=TRUE&id=" + event.target.getAttribute("value") + "&quantity=" + event.target["0"].value,
	    success: (resultData) => handleUpdateDisplayData(resultData), 
	    error: function (request, status, error) {
	   
	    }
	});
	
}

/*
 * Called when a delete button is pressed on cart.html 
 * Proceeds to delete the movie from the cart
 */
function deleteMovieFromCart(element){
	console.log(element.value);
	
	jQuery.ajax({
		dataType: "json",
	    method: "GET", 
	    url: "api/cart?deleteId=" + element.value,
	    success: (resultData) => displayCart(resultData), 
	    error: function (request, status, error) {
	    }
	});
}

/*
 * Called after loading data from CartServlet. Displays the movies in the cart and 
 * their quantities 
 * 
 * If there are no movies in the cart, json sent as: {"servlet_response_no_movies": 1}
 */
function displayCart(json) {
	var items = $("#item_list");
	items.html("");
	
	// CHANGE CODE HERE TO DISPLAY "NO MOVIES"
	if(json["servlet_response_no_movies"]) {
		console.log("No movies in cart");
		
		$("#checkout-button").hide();

		return;
	}
	
	let res = "";
	console.log(json.length);
	for(let i = 0; i < json.length; ++i) {
		var id = json[i]["id"];
		var title = json[i]["title"];
		var quantity = json[i]["quantity"];
		res+="<div class=\"item-list\">"
		res += "<form METHOD=\"get\" onsubmit=\"handleUpdateCart(event)\" value=\"" + id + "\"class=\"cart\">";
		res += "<strong>Movie Title: </strong>" + title + "<label> <strong>Quantity</strong> </label>" 
		res += "<input type=\"number\" min=\"0\" value=" + quantity + ">";
		res += "<input type=\"submit\" value=\"Update Quantity\">"; 
		res += "</form>";
		res += "<button class=\"deleteFromCart\" type=\"button\" onclick=\"deleteMovieFromCart(this)\" value=\"" +
		id + "\"> Delete </button>";
		res+="</div><br>"
	}
	
	items.append(res);
	$("#checkout").show();
}


/*
 * This function is called when the user fills in their credit card information and
 * checks out
 */
function submitCheckoutForm(event) {
	event.preventDefault();
	var checkout_info = $("#checkout_form").serialize();
	jQuery.ajax({
		dataType: "json",
	    method: "POST", 
	    url: "api/checkout",
	    data: checkout_info,
	    success: (json) => {
	    	if(json["fail"]){
	    		$("#display-invalid").show();
	    	}
	    	else{
	    		$("#display-invalid").hide();
	    		jQuery.ajax({
	    			method: "GET",
	    			url: "api/checkout?confirmation=TRUE",
	    			success: () => {
	    				window.location.href = "confirmation.html";
	    			}
	    		});	
	    	}
	    }, 
	    error: function (request, status, error) {
	    }
	});
}

$.ajax({
	type: "POST",
	url: "api/cart",
	success: function(resultDataString){
		handleSessionData(resultDataString);			
	}
});
	
jQuery.ajax({
	dataType: "json", // Setting return data type
	method: "GET", // Setting request method
	url: "api/cart", // Setting request url, which is mapped by StarsServlet in Stars.java
	success: (resultData) => displayCart(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
	error: function (request, status, error) {
		    
	}
});


$("#checkout-button").on('click', ()=> {
	console.log('hi')
	$(".checkout-modal").show().animate({opacity:1},300);
});


$('.checkout-modal').on('click', (event)=>{
	if(event.target.className === 'checkout-modal'){
		$('.checkout-modal').animate({opacity:0},300, function(){
			$('.checkout-modal').hide();
		});;
	}
});

$("#checkout_form").submit((event) => submitCheckoutForm(event));
