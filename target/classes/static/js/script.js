console.log("this is scripting file");

const toggleSidebar = () => {
	if ($(".sidebar").is(":visible")){
		//true  
		//if sidebar is open then close it
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
		
	} else {
		//false
		//show sidebar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};

const search = () => {
	//console.log("serching...");
	
	let query = $("#search-input").val();
	
	if(query == ""){
		$(".search-result").hide();
		
	}else{
		//serach
		console.log(query);
		$(".search-result").show();
	}

};