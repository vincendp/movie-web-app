function handleAddMovie(event) {
	event.preventDefault();
	var parameters = $("#movie-form").serialize();
	jQuery.ajax({
		dataType: "json",
	    method: "GET", 
	    url: "api/insert-new-stars-and-movies?" + parameters + "&movies=TRUE",
	    success: (data) => {
	    	console.log(data);
	    	var container = $("#movie-status");
	    	var movie_message = data["movie_message"];
	    	var genre_message = data["genre_message"];
	    	var star_message = data["star_message"];
	    	var html = movie_message + "<br>" + genre_message + "<br>" + star_message;
	    	container.html("");
	    	container.append(html);
	    },
	    error: function (request, status, error) {
	    }
	});
}

function handleAddStar(event) {
	event.preventDefault();
	var parameters = $("#star-form").serialize()
	jQuery.ajax({
		dataType: "json",
	    method: "GET", 
	    url: "api/insert-new-stars-and-movies?" + parameters,
	    success: (data) => {
	    	console.log(data);
	    	var container = $("#star-status");
	    	container.html("");
	    	container.append(data["message"]);
	    },
	    error: function (request, status, error) {
	    }
	});
}

function displayMetadata (json) {
	var container = $("#metadata-container");
	var html = "";
	for( var i = 0; i<json.length; ++i) {
		var table = json[i]["table"];
		var fields = json[i]["fields"];
		
		html += "<p>Table: " + table + "</p>";
		
		for( var j=0; j<fields.length; ++j) {
			html += "<p>Field: " + fields[j]["field"] + "  Data Type: " + fields[j]["type"] + "<p>";
		}
		
		html += "<br>";
	}
	container.append(html);
}


jQuery.ajax({
		dataType: "json",
	    method: "GET", 
	    url: "api/metadata",
	    success: (json) => displayMetadata(json),
	    error: function (request, status, error) {
	    }
	});

$("#star-form").submit((event) => handleAddStar(event));
$("#movie-form").submit((event) => handleAddMovie(event));