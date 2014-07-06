var nativePageClassName = null;

var accessToken = null;

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
	alert("Received phone number is: " + data.phoneNumber);
	accessToken = data.phoneNumber;
}
