# Pretty printer JavaScript API Documentation

## About

The Java application will load a .js file, create a context and execute the source code. The functions defined in the source code will be accessible to the Java program. There is a set of predefined function names that have to be used in order to customize the pretty printer. They are described in the section [Functions](#functions). There will be a description and an example for each of these functions.

## Functions

The goal of each JavaScript function is to customize a markdown element to your likings. The functions will have the original string as parameter, it can be modifed and returned. Or you could just provide and return a new string. There is one function that behaves differently, see [setDefaults](#setDefaults). The parameter of the function will contain one element of markdown, e.g. a header `### Header`. For each markdown element you can provide a function and modify the string. Each function has 1 parameter, in the following examples this is always declared as `params` but it can be anything else.

## Function list

  - [setDefaults](#setDefaults)
  - [processLineSpacing](#processLineSpacing)
  - [processHeader](#processHeader)
  - [processText](#processText)
  - [processBlockQuote](#processBlockQuote)
  - [processOrderedList](#processOrderedList)
  - [processBulletList](#processBulletList)
  - [processEmphasis](#processEmphasis)
  - [processStrongEmphasis](#processStrongEmphasis)
  - [processCode](#processCode)
  - [processIndentedCodeBlock](#processIndentedCodeBlock)
  - [processFencedCodeBlock](#processFencedCodeBlock)
  - [processThematicBreak](#processThematicBreak)
  - [processLinkText](#processLinkText)
  - [processLink](#processLink)
  - [processImageText](#processImageText)
  - [processImage](#processImage)

### setDefaults

##### Description

Function name: `setDefaults(params)`
Function return: `void`

This is a special function where you can set default values and symbols to use later on. These are the following:

  - `maxLineWidth`, maximum amount of characters per line, default value `100`
  - `bulletSymbol`, bullet item symbol, default value `-`
  - `orderedListDelimiter`, delimiter symbol in numbered list, default value `.`
  - `codeBlockSymbol`, fenced code block symbol, default value `~`
  - `emphasisSymbol`, emphasis (italic) symbol, default value `*`
  - `strongEmphasisSymbol` strong emphasis (bold) symbol, default value `**`
  - `codeSymbol`, code symbol, default value `` ` ``
  - `headingSymbol`, heading symbold, default value `#`
  - `blockQuoteSymbol`, block quote symbol,default value `>`

All these fields can be access like this: e.g. `params.bulletSymbol`

##### Example

objective: modify default heading symbol and set max line width to 50

description: these object fiels can be directly assigned with the desired value

params:

```js
params = {
    maxLineWidth: 100,
    bulletSymbol: '-',
    orderedListDelimiter: '.',
    codeBlockSymbol: '~',
    emphasisSymbol: '*',
    strongEmphasisSymbol: '**',
    codeSymbol: '`',
    headingSymbol: '#',
    blockQuoteSymbol: '>'
}
```

```js
function setDefaults(params) {
    params.headingSymbol = '@';
    params.maxLineWidth = 50;
}
```

***

### processLineSpacing

##### Description

Function name: `processLineSpacing(params)`
Function return: `string`

This function is used to modify line spacing. Normally `'\n'` will be passed from Java.

##### Example

objective: double line spacing

description: to each new line another one will be added

params: `'\n'`

```js
function processLineSpacing(params) {
    return params + '\n';
}
```

result: `'\n\n'`

***

### processHeader

##### Description

Function name: `processHeader(params)`
Function return: `string`

This function is used to modify the headers.

##### Example

objective: convert all letters to uppercase

description: call `toUpperCase()` function on passed string to make it uppercase

params: `'## Header'`

```js
function processHeader(params) {
    return params.toUpperCase();
}
```

result: `'## HEADER'`

***

### processText

##### Description

Function name: `processText(params)`
Function return: `string`

This function is used to modify normal text.

##### Example

objective: convert all letters to lowercase

description: call `toLowerCase()` function on passed string to make it lowercase

params: `'This is some text.'`

```js
function processText(params) {
    return params.toLowerCase();
}
```

result: `'this is some text.'`

***

### processBlockQuote

##### Description

Function name: `processBlockQuote(params)`
Function return: `string`

This function is used to modify block quotes.

##### Example

objective: mark ending of each block quote line

description: append the string `'---END_BLOCK_QUOTE'` to each block quote line

params: `'> This is a block quote'`

```js
function processBlockQuote(params) {
    return params += '---END_BLOCK_QUOTE';
}
```

result: `'> This is a block quote---END_BLOCK_QUOTE'`

***

### processOrderedList

##### Description

Function name: `processOrderedList(params)`
Function return: `string`

This function is used to modify ordered list items.

##### Example

objective: add space between each character of a list item

description: after each char a space `' '` is added

params: `'1. list item'`

```js
function processOrderedList(params) {
    let result = '';
    for(let i=0; i < params.length; i++) {
        result += params[i] + ' ';
    }
    return result;
}
```

result: `'1 .   l i s t   i t e m '`

***

### processBulletList

##### Description

Function name: `processBulletList(params)`
Function return: `string`

This function is used to modify bullet list items.

##### Example

objective: append length of each item

description: to each bullet item its length is append at the back with 

params: `'- banana'`

```js
function processBulletList(params) {
    return params + ' (' + params.length + ')';
}
```

result: `'- banana (8)'`

***

### processEmphasis

##### Description

Function name: `processEmphasis(params)`
Function return: `string`

This function is used to modify italic elements.

##### Example

objective: shorten long italic words

description: if an italic element is longer than 5 character it is converted to `'...'`

params: `'an italic item'`

```js
function processEmphasis(params) {
    let result = '';
    if(params.length > 5) {
        result = '...';
    } else {
        result = params;
    }
    return result;
}
```

result: `'...'`

***

### processStrongEmphasis

##### Description

Function name: `processStrongEmphasis(params)`
Function return: `string`

This function is used to modify bold elements.

##### Example

objective: shorten long bold words

description: if an bold element is longer than 5 character it is converted to `'...'`

params: `'a bold item'`

```js
function processStrongEmphasis(params) {
    let result = '';
    if(params.length > 5) {
        result = '...';
    } else {
        result = params;
    }
    return result;
}
```

result: `'...'`

***

### processCode

##### Description

Function name: `processCode(params)`
Function return: `string`

This function is used to modify code elements.

##### Example

objective: censor all code elements 

description: all code elements are censored by replacing them with `'CODE'`

params: `'sudo apt-get install secret-software'`

```js
function processCode(params) {
    return 'CODE';
}
```

result: `'CODE'`

***

### processIndentedCodeBlock

##### Description

Function name: `processIndentedCodeBlock(params)`
Function return: `string`

This function is used to modify indented code blocks.

##### Example

objective: replace tab characters with 4 spaces

description: find tab characters in the code block and replace them by 4 spaces

params:

```
    # Let me re-iterate ...
    for i in 1 .. 10 {
    	do-something(i)
    }
```

```js
function processIndentedCodeBlock(params) {
    var result = params.replace(/\t/g, '    ');
    return result;
}
```

result:

```
    # Let me re-iterate ...
    for i in 1 .. 10 {
        do-something(i)
    }
```

***

### processFencedCodeBlock

##### Description

Function name: `processFencedCodeBlock(params)`
Function return: `string`

This function is used to modify fenced code blocks.

##### Example

objective: replace tab characters with 4 spaces

description: find tab characters in the code block and replace them by 4 space

params:

```
~~~
define foobar() {
	print "Welcome to flavor country!";
}
~~~

```

```js
function processFencedCodeBlock(params) {
    var result = params.replace(/\t/g, '    ');
    return result;
}
```

result:

```
~~~
define foobar() {
    print "Welcome to flavor country!";
}
~~~
```

***

### processThematicBreak

##### Description

Function name: `processThematicBreak(params)`
Function return: `string`

This function is used to modify a thematic break.

##### Example

objective: use a custom thematic break

description: replace the default thematic break with a custom `'XXXXXXXXXX'`

params: `'***'`

```js
function processThematicBreak(params) {
    return 'XXXXXXXXXX';
}
```

result: `'XXXXXXXXXX'`

***

### processLinkText

##### Description

Function name: `processLinkText(params)`
Function return: `string`

This function is used to modify the displayed text of a link.

##### Example

objective: change link text to a warning

description: change link text to `'WARNING: Click at your own risk!'`

params: `'Link'`

```js
function processLinkText(params) {
    return 'WARNING: Click at your own risk!';
}
```

result: `'WARNING: Click at your own risk!'`

***

### processLink

##### Description

Function name: `processLink(params)`
Function return: `string`

This function is used to modify the target of a link.

##### Example

objective: redirect if link is not in domain

description: redirect to other side if link does not point to a specific domain. In this case `unibe.ch`

params: `'(www.unsafe-url.com)'`

```js
function processLink(params) {
    let result = '';
    if(/unibe\.ch/.test(params)) {
        result = params;
    } else {
        result = '(https://www.unibe.ch)';
    }
    return result;
}
```

result: `'(https://www.unibe.ch)'`

***

### processImageText

##### Description

Function name: `processImageText(params)`
Function return: `string`

This function is used to modify the image text.

##### Example

objective: tag each image text to notice it as image

description: append to each image text the string `'[IMG]'` to recognize it quicker

params: `'flower'`

```js
function processImageText(params) {
    return params + '[IMG]';
}
```

result: `'flower[IMG]'`

***

### processImage

##### Description

Function name: `processImage(params)`
Function return: `string`

This function is used to modify the image link.

##### Example

objective: change all .jpg to .png images

description: redirect all images to the .png format

params: `'(example-image.jpg "An exemplary image")'`

```js
function processImage(params) {
    let result = params.replace('.jpg', '.png');
    return result;
}
```

result: `'(example-image.png "An exemplary image")'`
