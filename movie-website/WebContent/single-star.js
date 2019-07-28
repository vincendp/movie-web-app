/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
	//Show name, year of birth, a list of movies in which the star performed
	let starContainer = jQuery(".star-container");
	console.log(resultData);
	var starName = resultData[0]["star_name"];
	var starDOB = resultData[0]["star_dob"]
	jQuery(".star-nav-text").append(starName + ' Bio Page');
	jQuery(".bio").append("<p class=\"large-font\">"+starName+"</p>" +
			"<p style=\"font-size: 18px; margin-left: 15px;\">Born in: "+starDOB+"</p>")
	
	
	for(var i = 0; i < resultData.length; ++i){
		var movieId = resultData[i]["movie_id"];
		var movieName = resultData[i]["movie_title"];
		var movieYear = resultData[i]["movie_year"];
		jQuery(".star-movies").append("<div class=\"movie-container\" style=\"margin: 5px; width: 90%;\">" +
				"<div class=\"movie-data-container\">" +
				 "<a href=\"single-movie.html?id="+movieId+"\" class=\"movie-text\">"+ movieName + " ("+movieYear+ ")</a>"+
				"<p class=\"small-movie-text\">Director: "+resultData[i]["movie_director"]+"</p>" +
				"</div></div>")
	}


	
	
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');
console.log('attempting to fetch star with id = '+starId);

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});