/*
 *		######################
 *      #### EXPLANATION: ####
 * 		######################
 *      
 *      Each function has a parameter which is a JavaScirpt object.
 *      It has the fields text and options:
 *      
 *      - Text contains the String which can be modified.
 *      - Options contains additional information about the String,
 *        e.g. with a header the level of the header (1-6).
 *      
 *      Mostly the string will be only one line.
 *      So in a block quote e.g. the function is called for each line
 *      but modified for each line in the same way.
 *      
 * 		######################
 *      ## FUNCTION NAMES: ###
 * 		######################
 *      
 *		setDefault:					set default symbols and settings
 *      processLineSpacing:			modify empty space lines
 *      processHeader: 				modify the header
 *      processText:   				modify normal text
 *      processBlockQuote:			modify block quotes
 *      processOrderedList:			modify an ordered list
 *      processBulletList:			modify a bullet list
 *      processEmphasis:			modify emphasis
 *      processStrongEmphasis: 		modify strong emphasis
 *      processCode:				modify a code element
 *      processIndentedCodeBlock:	modify an indented code block (more than 1 line, whole block)
 *      processFencedCodeBlock:		modify a fenced code block (more than 1 line, whole block)
 *      processThematicBreak:		modify the separation line
 *      processLinkText:			modify the text of the link
 *      processLink:				modify the link
 *      processImageText:			modify the image text
 *      processImage:				modify the image link
 *      
 */
 
function setDefaults(params) {
 	console.log(params);
 	params = '@';
}

function processLineSpacing(params) {
	return params + '\n';
}

function processHeader(params) {
	let result = '';
	for(let i=0; i<params.length; i++) {
		result += params[i].toUpperCase() + ' ';
	}
	return result;
}

function processText(params) {
	let result = '';
	for(let i=0; i<params.length; i++) {
		result += params[i].toUpperCase();
	}
	return result;
}

function processBlockQuote(params) {
	let result = '';
	for(let i=0; i<params.length; i++) {
		result += params[i].toLowerCase();
	}
	return result;
}

function processOrderedList(params) {
	return params + '';
}

function processBulletList(params) {
	return params + ' (Bullet item)';
}

function processEmphasis(params) {
	let result = '';
	for(let i=0; i<params.length; i++) {
		result += params[i].toLowerCase();
	}
	return params + 'EMPH'; 
}

function processStrongEmphasis(params) {
	let result = '';
	for(let i=0; i<params.length; i++) {
		result += params[i].toLowerCase();
	}
	return params + 'STRONGEMPH'; 
}

function processCode(params) {
	return '`Some code`';
}

function processIndentedCodeBlock(params) {
	return params;
}

function processFencedCodeBlock(params) {
	return params;
}

function processThematicBreak(params) {
	return '------------------------------\n';
}

function processLinkText(params) {
	return 'LINK';
}

function processLink(params) {
	return '(www.unibe.ch)';
}

function processImageText(params) {
	return 'IMG';
}

function processImage(params) {
	return params;
}


