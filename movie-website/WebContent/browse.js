const letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	
function handleGenres(result) {
	result = result.slice(0,-1);
	var genres = result.split(",");
	
	var g_cont = $('.genre-container');
	for(var i = 0; i < genres.length; ++i){
		g_cont.append("<span class=\"browse-tag\">"+genres[i]+"</span>")
	}
	var bf_cont = $('.bf-container');
	for(var i = 0; i < letters.length; ++i){
		bf_cont.append("<span class=\"browse-tag\">"+letters[i]+"</span>")
	}
	
	$('.browse-tag').on('click', function(){
		var value = $(this).text();
		if (value.length < 2) {
			window.history.pushState("", "", "?orderBy=title&order=ASC&browse="+value);
		}
		else if (value.length >= 2) {
			window.history.pushState("", "", "?orderBy=movie&order=DESC&browse="+value);
		}
		
		jQuery.ajax({
		    method: "GET", 
		    url: "api/browse?letter="+value,
		    success: (resultData) => handleSearchResult(resultData), 
		    error: function (request, status, error) {
		    	console.log(status)
		    	console.log('there was an error')
		    }
		});
	});
}


jQuery.ajax({
	dataType:"text",
    method: "GET", 
    url: "api/browse?genres=TRUE",
    success: (result) => handleGenres(result),
    error: function (request, status, error) {
    	console.log(status)
    	console.log('there was an error')
    }
});


// modal
$('.browse-button').on('click', ()=>{
	$('.browse-modal').show().animate({opacity:1},300);;
});


$('.browse-modal').on('click', (event)=>{
	if(event.target.className === 'browse-modal'){
		$('.browse-modal').animate({opacity:0},300, function(){
			$('.browse-modal').hide();
		});;
	}
});