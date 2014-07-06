
/* JavaScript content from js/main.js in folder common */
var nativePageClassName = null;

var atValue = null;

function wlCommonInit(){
    $('#btnOpen').bind('click', openNativePage);
    
}

function openNativePage(){
	var params = {
			nameParam : 'hogehoge'
	};
	
	if (!nativePageClassName){
		alert("Please run application on an Android/iOS environment");
	} else {
		WL.NativePage.show(nativePageClassName, backFromNativePage, params);
	}
}

function backFromNativePage(data){
	console.log('backStart');
	console.log("Received phone number is: " + data.phoneNumber);
	atValue = data.phoneNumber;
	console.log('backEnd');
}

/* JavaScript content from js/main.js in folder android */
// This method is invoked after loading the main HTML and successful initialization of the Worklight runtime.
var nativePageClassName = "com.myapp.HelloNative";

function wlEnvInit(){
    wlCommonInit();
    // Environment initialization code goes here
}