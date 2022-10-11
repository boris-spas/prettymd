function processHeader(params) {
    let result = '';
    for(let i=0; i<params.length; i++) {
        result += params[i].toUpperCase() + ' ';
    }
    return result;
}
