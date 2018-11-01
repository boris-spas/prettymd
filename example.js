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

function processLineSpacing(params) {
	return params.text + '\n';
}

function processHeader(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toUpperCase() + ' ';
	}
	return result;
}

function processText(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toUpperCase();
	}
	return result;
}

function processBlockQuote(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toLowerCase();
	}
	return result;
}

function processOrderedList(params) {
	return params.text + '';
}

function processBulletList(params) {
	return params.text + ' (Bullet item)';
}

function processEmphasis(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toLowerCase();
	}
	return params.text + 'EMPH'; 
}

function processStrongEmphasis(params) {
	let result = '';
	for(let i=0; i<params.text.length; i++) {
		result += params.text[i].toLowerCase();
	}
	return params.text + 'STRONGEMPH'; 
}

function processCode(params) {
	return '`Some code`';
}

function processIndentedCodeBlock(params) {
	return params.text;
}

function processFencedCodeBlock(params) {
	return params.text;
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
	return params.text;
}


