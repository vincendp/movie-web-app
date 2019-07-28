const apiKEY = '73e3ed9a';
const URL = 'https://www.omdbapi.com/'

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

function addCartButtonsToMovie(element){
	console.log("movieId: " + element.value);
	
	jQuery.ajax({
	    method: "GET", 
	    url: "api/cart?movieId=" + element.value
	});
	
}

function handleSingleMovieResult(resultData) {
	
	
    let movieContainer = jQuery(".movie-container");
    let rowHTML = "";
    var id = resultData[0]["id"];
    var title = resultData[0]["title"];
    var year = resultData[0]["year"];
    var director = resultData[0]["director"];
    var genres = resultData[0]["genres"];
    console.log(genres);
    var stars = resultData[0]["stars"];
    var rating = resultData[0]["movie_rating"]
    
    console.log(genres);
    
    var imageURL = URL+'?t='+title+'&apikey='+apiKEY;
    
    jQuery.get(imageURL, (data)=>{
    	console.log(data)
    	var imageTag = '<span></span>';
    	var status = data['Response'];
    	var imageURL = data['Poster'];
    	if(status === 'True' && imageURL !== 'N/A'){
        	imageTag = '<img src=\"'+imageURL+'\"/>'
    	}
    	    	
    	var html_title =  "<p> <strong>Movie</strong>: " + title +  "</p>";
        var html_year = "<p> <strong>Year</strong>: " + year + "</p>";
        var html_director = "<p> <strong>Director</strong>: " + director + "</p>";
        
        var html_genres = "<p><strong>Genres</strong>: ";
        
        for (var i=0; i < genres.length; ++i){
        	html_genres += "<a href='index.html?browse=";
        	html_genres += genres[i];
        	html_genres += "'>"+genres[i]+"</a>";
        	if(i != genres.length-1){
        		html_genres += ", ";
        	}
        }
        
        html_genres += "</p>";
       

        var html_stars = "<p> <strong> Stars</strong>: ";
        for ( let i=0; i<stars.length; ++i) {
        	html_stars += 
           	 '<a href="single-star.html?id=' + stars[i]["star_id"] + '">'
           	 + stars[i]["star_name"] + " " + 
           	 '</a>';
        }
        html_stars += "</p>";
        
        var button = "<p> <button class=\"addToCart\" type=\"button\" onclick=\"addCartButtonsToMovie(this)\" value=\"" +
		id + "\"> Add to Cart </button> </p>";
        
        var html_rating = "<p> <strong>Rating</strong>: " + rating + "</p>"
        
        
        rowHTML += html_title + html_year + html_director + html_genres + html_stars + html_rating + imageTag + button;
        movieContainer.append(rowHTML);
    })
    
    
    
}



let movieId = getParameterByName("id");

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSingleMovieResult(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
    error: function (request, status, error) {
    	console.log(request.responseText)
    	console.log("THERE WAS AN ERROR")
    	console.log(status)
    	console.log(error)
        alert(request.responseText);
    }
});