function(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toUpperCase() + ' ';
	}
	return result;
}