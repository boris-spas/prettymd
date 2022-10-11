function setDefaults(params) {
    params.headingSymbol = '+';
    params.maxLineWidth = 20;
}

function processLineSpacing(params) {
    return '\n';
}

function processHeader(params) {
    let result = '';
    for(let i=0; i<params.length; i++) {
        result += params[i].toUpperCase();
    }
    return result;
}

function processText(params) {
    let result = '';
    for(let i=0; i < params.length; i++) {
        result += params[i];
        if (params[i] == '.' && params[i+1] != ' ') {
            result+=" ";
        }
    }
    return result;
}
