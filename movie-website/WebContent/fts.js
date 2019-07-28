const cache = {}; //search text -> json result
const ftscache = {};
(function bindListeners(){
	$('.fts-input').autocomplete({
		
	    lookup: function (query, doneCallback) {
	    		handleLookup(query, doneCallback)
	    },
	    onSelect: function(suggestion) {
	    		handleSelectSuggestion(suggestion)
	    },
	    minChars: 3,
	    deferRequestBy: 300,
	    triggerSelectOnValidInput: false
	});
	
	//bind pressing enter key to a handler function
	$('.fts-input').keypress(function(event) {
		// keyCode 13 is the enter key
		if (event.keyCode == 13) {
			// pass the value of the input box to the handler function
			handleNormalSearch($('.fts-input').val())
		}
	})
})();

function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
	window.history.pushState("", "", '?query='+query+'&limit=20&offset=0');
	if(ftscache[query]){
		console.log('retrieving data from cache for query '+query);
		console.log(JSON.parse(ftscache[query]));
		handleSearchResult(JSON.parse(ftscache[query]), true);
	}else{
		jQuery.ajax({
			'method': 'GET',
			'url': 'api/fts?title='+query+'&limit=20&offset=0',
			'success': data =>{
				console.log('retrieving data from server for query '+query);
				ftscache[query] = data;
				handleSearchResult(JSON.parse(data), true);
			}
		})
	}
	//15 3
	//29 15
}

function handleSelectSuggestion(query){
	console.log('user has selected');
	console.log(query);
	let id = query['data']['id'];
	let movieTitle = query['value'];
	window.location.href = '/movie-website/single-movie.html?id='+id;
}

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")

	if(cache[query]){ //if their search query has been cached
		console.log(query+' has been found in the cache.')
		let result = cache[query];
		console.log(result)
		doneCallback(result);
	}else{ 
		jQuery.ajax({
			"method": "GET",
			"url": "api/fts?title=" + escape(query)+'&limit=10&offset=0',
			"success": function(data) {
				handleLookupAjaxSuccess(data, query, doneCallback) 
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
	
}


function handleLookupAjaxSuccess(data, query, doneCallback) {
//	console.log("lookup ajax successful")
	var jsonData = JSON.parse(data);
	let suggestionsObject = []
	
	for(let i = 0; i < Math.min(jsonData.length, 10); ++i){
		suggestionsObject.push({
			'value': jsonData[i].title,
			'data': {'id': jsonData[i].id}
		})
	}
	console.log('retrieved data from server');
	console.log(suggestionsObject);
	cache[query] = { suggestions: suggestionsObject }; //add the result to the cache
	doneCallback( { suggestions: suggestionsObject } );
}
