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
 * CAN DO SOMETHING WITH THIS LATER. RESPONSE WHEN USER CLICKS ADD TO CART??????
 */
function handleData(result){
	$('.popup-container').show().animate({opacity:1},300);
	setTimeout(function(){
		$('.popup-container').animate({opacity:0},300, function(){
			$('.pop-container').hide();
		});
	}, 1000);
}

function addCartButtonsToMovie(element){	
	
	jQuery.ajax({
	    method: "GET", 
	    url: "api/cart?movieId=" + element.value,
	    success: (resultData) => handleData(resultData), 
	    error: function (request, status, error) {
	    }
	});
	
}

/*
 * This function takes in json data sent by the servlet and maps it as HTML
 * elements
 */

function handleSearchResult(json, fts=false) {
    resultDataJson = JSON.parse(JSON.stringify(json));

    var main = $('.main-container');
    
    main.empty();
    var loader=$('.loader');
    
    loader.show();
    for(var i = 0; i < json.length; ++i){
    	var id = json[i]['id'];
        var title = json[i]['title'];
        var year = json[i]['year'];
        var director = json[i]['director'];
        var genres = json[i]['genres']; // array
        var rating = json[i]['movie_rating'];
        var stars = json[i]['stars'];
                
        var result = '<div class="movie-container"><div class="movie-data-container">';
        
        var titleElement = '<a href="single-movie.html?id=' + json[i]['id'] + '"' + ' class="movie-text" style=" font-weight: bold">'+title+' ('+year+')</a>';
        
        var directorElement = '<p class="small-movie-text">Directors: '+director+'</p>';
        
        var starsString = '';

        for( let j =0; j < json[i]["stars"].length; j++) {
            starsString +=
                '<a href="single-star.html?id=' + json[i]["stars"][j]["star_id"] + '">'
                + json[i]["stars"][j]["star_name"] + " " +
                '</a>';
        }
        var starsElement = '<p class="small-movie-text">Stars: '+starsString+'</p>';
        
        var genresElement = '<p class="small-movie-text">Genres: ';
        
        for (var x=0; x < genres.length; ++x){
        	genresElement += "<a href='index.html?browse=";
        	genresElement += genres[x];
        	genresElement += "'>"+genres[x]+"</a>";
        	if(x != genres.length-1){
        		genresElement += ", ";
        	}
        }
        genresElement += "</p>";
                
        var buttonElement = "<button class=\"addToCart\" type=\"button\" onclick=\"addCartButtonsToMovie(this)\" value=\"" +
		id + "\"> Add to Cart </button>";
        
        var ratingsElement = '<p>'+rating+'</p>';
        
        result += titleElement + directorElement+ starsElement + genresElement + buttonElement+'</div>';
        result += '<div class="ratings-container">' +  ratingsElement  +'</div></div>';
        
        main.append(result);
        loader.hide();
        if(fts){
        	applyFTSPag();
        }else{
            applyPagination();
        }
    }

}
/*
 * When the search form is clicked, this function will send a get request to the
 * search servlet and retrieve data. This function maintains pagination (limit
 * and offset) by getting the parameters from the url (or setting it to default
 * values if not found in url) and appending the limit and offset parameters
 * onto the mysql query
 */
function submitSearchForm(formSubmitEvent){
	formSubmitEvent.preventDefault();
	var query_string = $("#search_form").serialize();
	var limit = getParameterByName('limit') ? parseInt(getParameterByName('limit')) : 20;
	var offset = 0;
	
	window.history.pushState("", "", "?limit="+limit+"&offset="+offset+ "&" + query_string);
	
	$.get(
	        "api/search?offset=" + offset +"&limit=" + limit,
	        query_string,
	        (resultDataString) => handleSearchResult(resultDataString)
	    );
}
/*
 * When the sort form is clicked, this function will send a get request to the
 * movie servlet and retrieve data. This function maintains pagination (limit
 * and offset) by getting the parameters from the url (or setting it to default
 * values if not found in url) and appending the limit and offset parameters
 * onto the mysql query
 */
function submitSortForm(formSubmitEvent) {
	event.preventDefault();
	var limit = getParameterByName('limit') ? parseInt(getParameterByName('limit')) : 20;
	var offset = 0;
	var browse = getParameterByName('browse');
	
	updateParameters();
	sort_selected = $("#sort_form option:selected").val().split(" ");
	orderBy = sort_selected[0];
	order = sort_selected[1];

	search_query = "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star 
					+ "&orderBy=" + orderBy + "&order=" + order;
	

	window.history.pushState("", "", "?limit=" + limit + "&offset=" + offset + search_query + "&browse="+browse);

	
	if ( !title && !year && !director && !star || (title ==="null" && title ==="null" && director ==="null" && star==="null")){
		jQuery.ajax({
		    dataType: "json", // Setting return data type
		    method: "GET", // Setting request method
		    url: "api/movies?offset="+offset+"&limit="+limit+ "&orderBy=" + orderBy + "&order=" + order +"&browse="+browse, 
		    success: (resultData) => handleSearchResult(resultData), 
		    error: function (request, status, error) {
		    
		    }
		});
	}
	
	else if ( title || year || director || star) {
		jQuery.ajax({
		    dataType: "json", 
		    method: "GET", 
		    url: "api/search?offset="+offset+"&limit="+limit + search_query, 
		    success: (resultData) => handleSearchResult(resultData), 
		    error: function (request, status, error) {
		   
		    }
		});
	}
	
}

function applyFTSPag(){
	let cont = jQuery(".pagination-container")
	cont.empty(); // empty out html before reapplying it
	
	cont.append("<button class=\"pagination-tag\">Previous</button>");
	cont.append("<button class=\"pagination-tag\">Next</button>");
	
	$('.pagination-tag').on('click', function(){
		var value = $(this).text()
		var query = getParameterByName('query');

		var currentLimit = getParameterByName('limit') ? parseInt(getParameterByName('limit')) : 20;
		
		if(value === 'Next'){
			var offset = getParameterByName('offset') ? parseInt(getParameterByName('offset')) : 0;
			offset += currentLimit
		}else if (value === 'Previous'){
			var offset = getParameterByName('offset') ? parseInt(getParameterByName('offset')) : 0;
			offset = offset - currentLimit < 0 ? 0 : offset - currentLimit
		}else{
			var offset = (value-1)*currentLimit;
		}
		window.history.pushState("", "", '?query='+query+'&limit='+currentLimit+'&offset='+offset);

		jQuery.ajax({
			'datatype':'json',
			'method':'GET',
			'url':'api/fts?title='+query+'&limit='+20+'&offset='+offset,
			'success': (data)=>{
				handleSearchResult(JSON.parse(data), true);
			}
		});
		
	});

	
	
	
	
}

function applyPagination(){
    
	var cont = jQuery(".pagination-container")
	cont.empty(); // empty out html before reapplying it
	
	cont.append("<button class=\"pagination-tag\">Prev</button>");
	cont.append("<button class=\"pagination-tag\">Next</button>");
	
	$('.pagination-tag').on('click', function(){
		var value = $(this).text()
		var currentLimit = getParameterByName('limit') ? parseInt(getParameterByName('limit')) : 20;
		var browse = getParameterByName('browse');
		
		if(value === 'Next'){
			var offset = getParameterByName('offset') ? parseInt(getParameterByName('offset')) : 0;
			offset += currentLimit
		}else if (value === 'Prev'){
			var offset = getParameterByName('offset') ? parseInt(getParameterByName('offset')) : 0;
			offset = offset - currentLimit < 0 ? 0 : offset - currentLimit
		}else{
			var offset = (value-1)*currentLimit;
		}
		
		updateParameters();
		
		if ( !title && !year && !director && !star || (title ==="null" && title ==="null" && director ==="null" && star==="null")){
			window.history.pushState("", "", "?limit=" + currentLimit+"&offset="+offset+ "&orderBy=" + orderBy + "&order=" + order+"&browse="+browse);
			
			jQuery.ajax({ // movie servlet
			    dataType: "json", // Setting return data type
			    method: "GET", // Setting request method
			    url: "api/movies?orderBy=" + orderBy + "&order=" + order + "&limit=" + currentLimit + "&offset=" + offset+"&browse="+browse, 
			    success: (resultData) => handleSearchResult(resultData), 
			    error: function (request, status, error) {
			    }
			});
			}

			else if ( title || year || director || star) {
				window.history.pushState("", "", "?limit=" + currentLimit+"&offset="+offset + search_query);
				
				jQuery.ajax({ // search servlet
				    dataType: "json", 
				    method: "GET", 
				    url: "api/search?" + "limit=" + currentLimit + "&offset=" + offset + "&orderBy=" + orderBy + "&order=" + order + search_query, 
				    success: (resultData) => handleSearchResult(resultData), 
				    error: function (request, status, error) {
				   
				    }
				});
			}
	});
}

function start(){
	
	$("#search_form").submit((event) => submitSearchForm(event));
	$("#sort_form").on('change', function(event) {
		submitSortForm(event);
	});
	$(".limit-selector").on('change', function(){
		limit = $(this).val();
		var offset = getParameterByName('offset');
		if(!offset){ // set offset to 0 by default
			offset = 0;
		}
		updateParameters();
		var browse = getParameterByName("browse");
		if ( !title && !year && !director && !star || (title ==="null" && title ==="null" && director ==="null" && star==="null")){
			window.history.pushState("", "", "?limit=" + limit+"&offset="+offset+ "&orderBy=" + orderBy + "&order=" + order+"&browse="+browse);
			
			jQuery.ajax({ // movie servlet
			    dataType: "json", // Setting return data type
			    method: "GET", // Setting request method
			    url: "api/movies?orderBy=" + orderBy + "&order=" + order + "&limit=" + limit + "&offset=" + offset+"&browse="+browse, 
			    success: (resultData) => handleSearchResult(resultData), 
			    error: function (request, status, error) {
			    }
			});
			}

			else if ( title || year || director || star) {
				window.history.pushState("", "", "?limit=" + limit+"&offset="+offset + search_query);
				
				jQuery.ajax({ // search servlet
				    dataType: "json", 
				    method: "GET", 
				    url: "api/search?" + "limit=" + limit + "&offset=" + offset + "&orderBy=" + orderBy + "&order=" + order + search_query, 
				    success: (resultData) => handleSearchResult(resultData), 
				    error: function (request, status, error) {
				   
				    }
				});
			}
	
	});

	updateParameters();
	var browse = getParameterByName("browse");
	if (browse){
		jQuery.ajax({ 
		    dataType: "json", 
		    method: "GET", 
		    url: "api/browse?letter="+browse,
		    success: (resultData) => handleSearchResult(resultData), 
		    error: function (request, status, error) {
		    
		    }
		});
	}
	else if ( !title && !year && !director && !star || (title ==="null" && title ==="null" && director ==="null" && star==="null")){
		jQuery.ajax({ // movie servlet
	    dataType: "json", // Setting return data type
	    method: "GET", // Setting request method
	    url: "api/movies?orderBy=" + orderBy + "&order=" + order, 
	    success: (resultData) => handleSearchResult(resultData), 
	    error: function (request, status, error) {
	    
	    }
	});
	}

	else if ( title || year || director || star) {
		jQuery.ajax({ // search servlet
		    dataType: "json", 
		    method: "GET", 
		    url: "api/search?" + "orderBy=" + orderBy + "&order=" + order + search_query, 
		    success: (resultData) => handleSearchResult(resultData), 
		    error: function (request, status, error) {
		   
		    }
		});
	}
	
	
}

function updateParameters() {
	title = getParameterByName("title");
	year = getParameterByName("year");
	director = getParameterByName("director");
	star = getParameterByName("star");
	orderBy = getParameterByName("orderBy");
	order = getParameterByName("order");
	search_query = "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star
						+"&orderBy=" + orderBy + "&order=" + order;
}


var title;
var year;
var director;
var star;
var orderBy;
var order;
var search_query;


start();
applyPagination();
$('.search-button').on('click', ()=>{
	$('.search-modal').show().animate({opacity:1},300);
});


$('.search-modal').on('click', (event)=>{
	if(event.target.className === 'search-modal'){
		$('.search-modal').animate({opacity:0},300, function(){
			$('.search-modal').hide();
		});;
	}
});

// let letter = getParameterByName("browse");
//
// jQuery.ajax({
// dataType: "json", // Setting return data type
// method: "GET", // Setting request method
// url: "api/browse?letter=" + letter , // Setting request url, which is mapped
// by StarsServlet in Stars.java
// success: (resultData) => handleSearchResult(resultData), // Setting callback
// function to handle data returned successfully by the StarsServlet
// error: function (request, status, error) {
// console.log('error browsing')
// }
// });
  




