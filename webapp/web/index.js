var baseUrl = 'api/';

window.$ = window.jQuery = $;

$.ajaxSetup({
    headers: {
        'auth-token': "guest"
    }
});

// import handlebars;
var container = document.getElementById('test-hot');

// var searchFiled = document.getElementById('search_field');
var data;
var selectedRange;
var bId;
var sName;
var range;
var bName;
var sheetNames = [];
var sheetData;
var currentSheet;

var firstVisibleRowVscroll = 0;
var lastVisibleRowVscroll = 0
// mock service for getting data
var fetchData = function (n) {
    return Handsontable.helper.createSpreadsheetData(n, 20);
};

var testData = fetchData(1000);


//dynamic scrolling and windowing:1000rows
var compute_window = function (e) {
    var rowCount = hot.countRows();
    var rowOffset = hot.rowOffset();
    var visibleRows = hot.countVisibleRows();
   // console.log("rowCount: "+rowCount)
   // console.log("rowOffset: "+rowOffset);
   // console.log("visibleRows: "+visibleRows);
   // console.log("countRenderedRows: "+hot.countRenderedRows());

    var lastRow = rowOffset + (visibleRows * 1);
    var lastVisibleRow = rowOffset + visibleRows + (visibleRows / 2);
    var threshold = 15;
    // $(".parallax-one").css({   //progress bar
    //     height: ((lastRow / currRange) * 80) + "%"
    // });
   // console.log("lastVisibleRow: "+lastVisibleRow);
    if (lastVisibleRow > upperRange - threshold) {
        updateData(upperRange, 0, upperRange + 1000, 15, false)
        upperRange = upperRange + 1000;
        console.log("in compute window");
    }

    if (rowOffset < lowerRange - threshold) {
        updateData(rowOffset - 200, 0, rowOffset, 15, false)
        lowerRange = lowerRange - 200;
        console.log("in compute window");
    }

    // if (lastVisibleRow > (rowCount - threshold)) {
    //   loadMoreData(rowCount);
    //  }
    if(nav!=undefined)
    {
        brushNlink(lastVisibleRow-visibleRows,lastVisibleRow);
    }
};

// load data and render
var loadMoreData = function (n) {
    // call data service
    var incoming = fetchData(n);
    var emptyArray = Array(50).fill(null);

    incoming.forEach(function (d) {
        testData.push(d);
    });
    hot.render();
};

var wrapperHeight = $(".wrapper").height();
var wrapperWidth = $(".wrapper").width();

//default setting
var ssDefaultSettings = {
    minRows: 200,
    minCols: 50,
    // startRows: 200,
    startCols: 40,
    //width: 1200,
    //height: 800,
    width: $(".wrapper").width(),
    height: wrapperHeight,

    rowHeaders: true,
    colHeaders: true,
    //currentRowClassName: 'currentRow',
    //currentColClassName: 'currentColum',
    //fixedRowsTop: 1,
    contextMenu: true,
    outsideClickDeselects: false,
    manualColumnResize: true,
    manualRowResize: true,
    search: true,
    sortIndicator: true,
    customBorders: true,
    fixedRowsTop: 1,
    // contextMenu:[],
    afterScrollVertically: function (e) {
        compute_window(e);
        console.log("scroll down");

    },
    // data: tempdata,
    // columns: [{renderer: chartRenderer}],
    // afterChange: function (change, source) {
    //     var updatedData = [];
    //     console.log(change)
    //     console.log(source)
    //     if (change !== null) {
    //         change.forEach(function (e) {
    //             if (e[3].charAt(0) == '=') {
    //                 updatedData.push({
    //                     "row": e[0],
    //                     "col": e[1],
    //                     "value": '',
    //                     "formula": e[3].substring(1),
    //                     "type": 'String',
    //                     "format": ''
    //                 });
    //
    //             } else {
    //                 updatedData.push({
    //                     "row": e[0],
    //                     "col": e[1],
    //                     "value": e[3],
    //                     "formula": '',
    //                     "type": '',
    //                     "format": ''
    //                 });
    //             }
    //
    //         })
    //     }
    //
    //     var update = {
    //         'bookId': bId,
    //         'sheetName': sName,
    //         'range': range,
    //         'cells': updatedData
    //     };
    //     if (!(update.cells.length == 0)) {
    //         $.ajax({
    //             url: baseUrl + "putCells",
    //             method: 'PUT',
    //             headers: {
    //                 'auth-token': 'guest'
    //             },
    //             dataType: "json",
    //             contentType: "application/json",
    //             data: JSON.stringify(update)
    //         })
    //     }
    //
    // },
    beforeSetRangeEnd: function (e) {


    }
    // ,
    // afterScrollVertically: function(e){
    //     compute_window(e);
    // }
    // ,data: testData
};

var SFU = false;
//dynamic setting: fix window width based on screen resolution,
// putCell afterChange() and load()
//handle formula execution and show result
var ssDynamicSettings = {
    minRows: 200,
    minCols: 50,
    // startRows: 200,
    startCols: 40,
    //width: 1200,
    //height: 800,
    width: $(".wrapper").width(),
    height: wrapperHeight,
    rowHeaders: true,
    colHeaders: true,
    contextMenu: true,
    outsideClickDeselects: false,
    manualColumnResize: true,
    manualRowResize: true,
    search: true,
    sortIndicator: true,
    customBorders: true,
    fixedRowsTop: 1,
    // contextMenu:[],
    afterScrollVertically: function (e) {
        compute_window(e);
        console.log("scroll down");
    }
    ,
    afterSelection: function (row, column, row2, column2, preventScrolling, selectionLayerLevel) {
        console.log("afterSelection")
        console.log(row, column)
        $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + row + "/" + column + "/" + row + "/" + column, function (data) {

            console.log(data)
            console.log(data.data.cells[0].formula)
            if (data.data.cells[0].formula != "") {

                $("#formulaBar").val("=" + data.data.cells[0].formula);
            }
            else {
                $("#formulaBar").text = "";
            }
        })

    },
    afterChange: function (change, source) {
        var updatedData = [];
        //alert(source);
        if (source == "populateFromArray") {
            return;
        }
        let formulaCellList = [];
        if (!SFU) {
            if (change !== null) {
                change.forEach(function (e) {
                    console.log(e);
                    if (e[3].charAt(0) == '=') {
                        updatedData.push({
                            "row": e[0],
                            "col": e[1],
                            "value": '',
                            "formula": e[3].substring(1),
                            "type": 'String',
                            "format": ''
                        });
                        formulaCellList.push({
                            "row": e[0],
                            "col": e[1],
                            "value": '',
                            "formula": e[3].substring(1),
                            "type": 'String',
                            "format": ''
                        });

                    } else {
                        updatedData.push({
                            "row": e[0],
                            "col": e[1],
                            "value": e[3],
                            "formula": '',
                            "type": '',
                            "format": ''
                        });
                    }

                })
            }

            var update = {
                'bookId': bId,
                'sheetName': sName,
                'range': range,
                'cells': updatedData
            };
            if (!(update.cells.length == 0)) {
                $.ajax({
                    url: baseUrl + "putCells",
                    method: 'PUT',
                    headers: {
                        'auth-token': 'guest'
                    },
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(update)
                }).done(function (result) {
                    if (formulaCellList.length != 0) {
                        let r1 = formulaCellList[0].row;
                        let c1 = formulaCellList[0].col;
                        $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + r1 + "/" + c1 + "/" + r1 + "/" + c1, function (data) {
                            SFU = true;
                            hot.setDataAtCell(r1, c1, data.data.cells[0].value);
                        })
                    }
                })
            }
        } else {
            SFU = false;
        }


    },
    beforeSetRangeEnd: function (e) {


    }
    // ,
    // afterScrollVertically: function(e){
    //     compute_window(e);
    // }
    // ,data: testData
};
// //initializing interface
var hot = new Handsontable(container, ssDefaultSettings); //show blank sheet


var clearCanvas = function (dataArray) {
    if (dataArray != undefined) {
        ssDynamicSettings.data = dataArray;
        hot = new Handsontable(container, ssDynamicSettings);
    } else {
        hot = new Handsontable(container, ssDefaultSettings);

    }
}


var getSheets = function () {
    //get all the sheet name and id and generfate button for each of them
    $.get(baseUrl + "getSheets/" + bId, function (data) {
        //add sheet name to tabs
        var sheets = data['data']['sheets'];
        sName = sheets[0].name;

        // $('#sheets').empty();
        sheets.forEach(function (e) {

            console.log(e);

            $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
        });

        $("#tableName").text($('#book-selector').val());
        sheetData = data;
    })
}

var renderSheets = function (sheets) {
    $("#sheets").empty();
    sheets.forEach(function (e) {
        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e['name'] + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item" href="#">Rename</a><a class="dropdown-item" href="#">Duplicate</a><a class="dropdown-item" href="#">Delete</a><a class="dropdown-item" href="#">Clear</a><a class="dropdown-item" href="#">Move Left</a><a class="dropdown-item" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
    });
}

var reorderSheets = function () {

}

var createSheet = function () {
    //create a new sheet and by default ask people to specify the name of the sheet
    $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + $('#createdSheetName').val() + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

    bindSheetListener();

    sheetNames.push($('#createdSheetName').val());
    sName = $('#createdSheetName').val();
    var update = {
        "bookId": bId,
        "sheetName": sName
    }
    //
    $.ajax({
        url: baseUrl + "addSheet",
        method: 'POST',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {

        $.get(baseUrl + "getSheets/" + bId, function (data) {
            //add sheet name to tabs
            // window.data01=data
            sheetData = data;

            openSheet(bId, sName, data['data']['sheets'].length);

        })
        // openSheet(bId, sheet, sName, sheetNames.length);

    })

}

var renameSheet = function (e) {
    //convert button to clickable field
    //
    currentSheet = e.target.parentNode.parentNode.children[0];
    sName = currentSheet.innerText;


    $("#sheet-dialog").empty();
    $('<label></label>').text("Rename the current Sheet:").appendTo($("#sheet-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "newSheetName").appendTo("#sheet-dialog");
    var dialog = $("#sheet-dialog").dialog({
        buttons: {
            "Change Name": function () {

                var update = {
                    "bookId": bId,
                    "oldSheetName": sName,
                    "newSheetName": $("#newSheetName").val()
                }


                $.ajax({
                    url: baseUrl + "changeSheetName",
                    method: 'PUT',
                    headers: {
                        'auth-token': 'guest'
                    },
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(update)
                }).done(function () {

                    sName = $("#newSheetName").val();


                    // currentSheet.innerText() = sName;
                    currentSheet.innerText = sName
                })

                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    })
}


var deleteSheet = function (e) {
    var currentSheet = e.target.parentNode.parentNode.children[0];
    e.target.parentNode.parentNode.remove();

    var update = {
        "bookId": bId,
        "sheetName": currentSheet.innerText
    }
    $.ajax({
        url: baseUrl + "deleteSheet",
        method: 'DELETE',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    })
}


var clearSheet = function (e) {

    sName = e.target.parentNode.parentNode.children[0].innerText;
    var update = {
        "bookId": bId,
        "sheetName": sName
    }

    $.ajax({
        url: baseUrl + "clearSheet",
        method: 'PUT',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {
        clearCanvas();
    });
}

var duplicateSheet = function (e) {

    sName = e.target.parentNode.parentNode.children[0].innerText;
    var update = {
        "bookId": bId,
        "sheetName": sName
    }

    $.ajax({
        url: baseUrl + "copySheet",
        method: 'POST',
        headers: {
            'auth-token': 'guest'
        },
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(update)
    }).done(function () {

        $.get(baseUrl + "getSheets/" + bId, function (data) {
            $('#sheets').empty();
            //add sheet name to tabs
            var sheets = data['data']['sheets'];
            sName = sheets[0].name;

            // $('#sheets').empty();
            sheets.forEach(function (e) {

                console.log(e);

                $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));
            });

            bindSheetListener();
            $("#tableName").text($('#book-selector').val());
            sheetData = data;

            openSheet(bId, sName, 0);

        })
    });

}

// var moveSheet = function(e, direction){
//
//     if (direction = "-1"){
//
//     }
//
//     sName = e.target.parentNode.parentNode.children[0].innerText;
//     var update = {
//       "bookId": bId,
//       "sheetName": sName,
//       "newPos":
//     }
//
//     $.ajax({
//       url: baseUrl+"moveSheet",
//       method:'PUT',
//       headers: {
//         'auth-token':'guest'
//       },
//       dataType:"json",
//       contentType:"application/json",
//       data: JSON.stringify(update)
//     })
//
// }

$("#addSheetButton").click(function () {
    $("#sheet-dialog").empty();
    $('<label></label>').text("Specify the name of the sheet(Unique in the book):").appendTo($("#sheet-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "createdSheetName").appendTo("#sheet-dialog");
    var dialog = $("#sheet-dialog").dialog({
        buttons: {
            "Create": function () {
                createSheet();
                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    })
})


var bindSheetListener = function () {

    $('.sheet>.btn.btn-secondary').click(function (e) {

        var target = e.target.parentNode.children[0]
        if ($(".btn-primary").length == 0) {
            $(target).attr('class', 'btn btn-primary');
            console.log("yes")
        } else {
            console.log("no")
            $(".btn-primary").attr('class', 'btn btn-secondary');
            $(target).attr('class', 'btn btn-primary');
        }

        sName = target.innerText;
        openSheet(bId, sName, 8)

    });

    $('.deleteSheet').click(function (e) {
        deleteSheet(e);
    });

    $(".renameSheet").click(function (e) {
        renameSheet(e);
    });

    $(".clearSheet").click(function (e) {
        clearSheet(e);
    })


    $(".moveLeftSheet").click(function (e) {

    })

    $(".moveRightSheet").click(function (e) {

    })

    $(".duplicateSheet").click(function (e) {
        duplicateSheet(e);
    })
}


var openSheet = function (bookId, sheetName, sheetIndex) {


    var r1 = 0;
    var c1 = 0;
    var r2 = 1000;
    var c2 = 50;
    sName = sheetName;

    $.get(baseUrl + "getCells/" + bookId + "/" + sheetName + "/" + r1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {

        // data['data']['cells'].forEach(function (e) {
        //     if (e.value !== 'null') {
        //         hot.setDataAtCell(e.row, e.col, e.value);
        //     }
        // })
        var testingarray = [];
        console.log(data)
        for (let i = r1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                if (data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value != "null") {
                    temp.push(data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value);
                } else {
                    temp.push("");
                }

            }
            testingarray.push(temp);
        }
        clearCanvas(testingarray);
    })

}


$("#rename-book").click(function () {
    renameBook();
});

$("#delete-book").click(function () {
    deleteBook();
});

$('#close-book').click(function () {
    closeBook();
});


$('#create-book').click(function () {
    $("#book-dialog").empty();

    $(function () {

        $('<label></label>').text("Create a new book:").appendTo($("#book-dialog"));
        $('<input type="text">').attr("value", '').attr("id", "createdBookName").appendTo("#book-dialog");

        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Create": function () {
                    var newBook = $('#createdBookName').val();
                    $.ajax({
                        url: baseUrl + "addBook",
                        method: "POST",
                        headers: {
                            "auth-token": "guest"
                        },
                        data: JSON.stringify({"name": newBook}),
                        dataType: "json",
                        contentType: "application/json"
                    }).done(function (result) {
                        clearCanvas();
                        $("#tableName").text(newBook);
                        bId = result['data']['book']['id'];
                        bName = result['data']['book']['name'];
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
});


var getBooks = function () {
    $.get(baseUrl + "getBooks", function (data) {
        // var i = 0;
        var workbooks = data['data']['books']
        workbooks.forEach(function (e) {
            $("<option></option>").text(e['name']).attr("id", e['id']).attr("link", e['link']).appendTo($('#book-selector'));
        });
        $('#book-selector').selectmenu();
        return data;
    })
}

$('#open-book').click(function () {

    $("#book-dialog").empty();
    $('<label></label>').text("Open a book:").appendTo($("#book-dialog"));
    $('<select></select>').attr("id", 'book-selector').appendTo($("#book-dialog"));

    $.get(baseUrl + "getBooks", function (data) {
        // var i = 0;
        var workbooks = data['data']['books']
        workbooks.forEach(function (e) {
            $("<option></option>").text(e['name']).attr("id", e['id']).attr("link", e['link']).appendTo($('#book-selector'));
        });
        $('#book-selector').selectmenu();
    })


    var dialog = $("#book-dialog").dialog({
        buttons: {
            "Confirm": function () {
                var bookName = $('#book-selector').prop('selectedIndex');
                var selectedBook = $('#book-selector>option')[bookName];

                var bookId = $(selectedBook).prop('id');

                bId = bookId;
                bName = $('#book-selector').val();


                $.get(baseUrl + "getSheets/" + bId, function (data) {
                    $('#sheets').empty();
                    //add sheet name to tabs
                    var sheets = data['data']['sheets'];
                    sName = sheets[0].name;

                    // $('#sheets').empty();
                    sheets.forEach(function (e) {

                        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

                    });

                    bindSheetListener();

                    $("#tableName").text($('#book-selector').val());
                    sheetData = data;

                    openSheet(bId, sName, 0);

                })

                dialog.dialog('close');
            },
            Cancel: function () {
                dialog.dialog('close');
            }
        }
    });
});


var renameBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Rename the book:").appendTo($("#book-dialog"));
        $('<input type="text">').attr("value", bName).attr("id", "newBookName").appendTo("#book-dialog");
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Change Name": function () {
                    bName = $("#newBookName").val()
                    var update = {
                        "bookId": bId,
                        "newBookName": bName
                    }
                    $.ajax({
                        url: baseUrl + "changeBookName",
                        method: 'PUT',
                        headers: {
                            'auth-token': 'guest'
                        },
                        dataType: "json",
                        contentType: "application/json",
                        data: JSON.stringify(update)
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })

}

var deleteBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Are you sure you want to delete the book:" + bName).appendTo($("#book-dialog"));
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Delete": function () {
                    var update = {"bookId": bId};
                    $.ajax({
                        url: baseUrl + "deleteBook",
                        method: 'DELETE',
                        headers: {
                            'auth-token': 'guest'
                        },
                        dataType: "json",
                        contentType: "application/json",
                        data: JSON.stringify(update)
                    }).done(function (result) {
                        clearCanvas();
                    })
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}

var closeBook = function () {
    $("#book-dialog").empty();
    $(function () {
        $('<label></label>').text("Are you sure you want to close the book:" + bName).appendTo($("#book-dialog"));
        var dialog = $("#book-dialog").dialog({
            buttons: {
                "Close": function () {
                    clearCanvas();
                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}


// var customizeHeader = function(){
//   // select range
//   var selected = hot.getSelected();
//
//   var r1 = selected[0];
//   var c1 = selected[1];
//   var r2 = selected[2];
//   var c2 = selected[3];
//
//   var i;
//   var headerNames = []
//   for (i=c1; i <= c2; i++){
//     var header = hot.getDataAtCell(r1, i);
//     $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
//     $("<p style='display: inline;'></p>").text(header).appendTo($('#'+header));
//     $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#'+header));
//
//     // headerNames.push()
//   }
//   // click create Table
//   // change style(add a circle)
//   // change header cells
// };


var createTable = function (range) {
    var r1 = range[0];
    var c1 = range[1];
    var r2 = range[2];
    var c2 = range[3];
    var i;

    var headerNames = []

    $("#table-dialog").empty();
    $("#table-dialog").attr("title", "Create a table")
    $('<label></label>').text("Specify the name of the table:").appendTo($("#table-dialog"));
    $('<input type="text">').attr("value", '').attr("id", "create-table-name").appendTo("#table-dialog");
    $('<label></label>').text("Specify the type of each column.").appendTo($("#table-dialog"));

    for (i = c1; i <= c2; i++) {
        var header = hot.getDataAtCell(r1, i);
        $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
        $("<p style='display: inline;'></p>").text(header).appendTo($('#' + header));
        $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#' + header));
    }
    // headerNames.push()

    // var headRow = $('.handsontable td.area').slice(c2-c1+1);
    // var bodyRows = $('.handsontable td.area').slice(0, c2-c1+1);
    // headRow.css("background","#2A7E43");
    // debugger
    // update spreadsheet setting
    hot.updateSettings({
        cells: function (row, col, prop) {
            var cellProperties;

            if (row === r1) {
                if (col >= c1 && col <= c2) {
                    // console.log(hot.getCellMeta(row, col));
                    var cell = hot.getCell(row, col);   // get the cell for the row and column
                    cell.style.backgroundColor = "#003F13";  // set the background color
                    cell.style.color = "#ffffff";
                }
            }

            if (row > r1 && row <= r2) {
                if (col >= c1 && col <= c2) {
                    var cell = hot.getCell(row, col);   // get the cell for the row and column
                    cell.style.backgroundColor = "#2A7E43";  // set the background color
                    cell.style.color = "#ffffff";
                }
            }

            return cellProperties;
        }
    });
    // bodyRows.css("background", "#003F13");

    // function firstRowRenderer(instance, td, row, col, prop, value, cellProperties) {
    //   Handsontable.renderers.TextRenderer.apply(this, arguments);
    //   td.style.fontWeight = 'bold';
    //   td.style.color = 'green';
    //   td.style.background = '#CEC';
    // }

    $(function () {

        var dialog = $("#table-dialog").dialog({
            buttons: {
                "Create Table": function () {
                    var tableName = $('#create-table-name').val();
                    var typeArray = []
                    $.each($("#table-dialog select"), function (index, value) {
                        typeArray.push($(value).val());
                    })

                    var table = {
                        'bookId': bId,
                        'sheetName': sName,
                        'tableName': tableName,
                        'row1': r1,
                        'col1': c1,
                        'row2': r2,
                        'col2': c2,
                        'schema': typeArray
                    }
                    console.log(table);

                    debugger

                    for (i = c1; i <= c2; i++) {
                        var header = hot.getDataAtCell(r1, i);
                        $("<div></div>").attr("id", header).appendTo($('#table-dialog'));
                        $("<p style='display: inline;'></p>").text(header).appendTo($('#' + header));
                        $("<select><option>TEXT</option><option>INTEGER</option><option>FLOAT</option></select>").addClass("alignRight").appendTo($('#' + header));
                    }


                    // $.ajax({
                    //   url:baseUrl+"createTable",
                    //   method:"POST",
                    //   headers:{
                    //     "auth-token":"guest"
                    //   },
                    //   data:JSON.stringify(table),
                    //   dataType:"json",
                    //   contentType:"application/json"
                    // }).done(function(result){
                    // })


                    dialog.dialog('close');
                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
}


$('#createTable').click(function () {
    range = hot.getSelected();
    createTable(range);


    // $('.handsontable td.area').css("background-color", "#F00");

    // $(function(){
    //   customizeHeader();
    //   var dialog = $("#table-dialog").dialog({
    //     buttons: {
    //       "Create Table": function(){
    //         range = hot.getSelected();
    //         var row1 = range[0];
    //         var col1 = range[1];
    //         var row2 = range[2];
    //         var col2 = range[3];
    //         var tableName = $('#create-table-name').val();
    //
    //         var typeArray = []
    //         $.each( $("#table-dialog select"), function(index, value){
    //           typeArray.push($(value).val());
    //         })
    //
    //
    //         var table = {
    //           'bookId':bId,
    //           'sheetName':sName,
    //           'tableName':tableName,
    //           'row1': row1,
    //           'col1': col1,
    //           'row2':row2,
    //           'col2':col2,
    //           'schema':typeArray
    //         }
    //
    //         $.ajax({
    //           url:baseUrl+"createTable",
    //           method:"POST",
    //           headers:{
    //             "auth-token":"guest"
    //           },
    //           data:JSON.stringify(table),
    //           dataType:"json",
    //           contentType:"application/json"
    //         }).done(function(result){
    //         })
    //
    //
    //
    //         dialog.dialog('close');
    //       },
    //       Cancel: function(){
    //         dialog.dialog('close');
    //       }
    //     }
    //   })
    // })

});

$(".fa.fa-underline").click(function () {
    $(".handsontable td.area").css("text-decoration", "underline");
})

$(".fa.fa-strikethrough").click(function () {
    $(".handsontable td.area").css("text-decoration", "line-through");
})

$(".fa.fa-bold").click(function () {
    $(".handsontable td.area").css("font", "bold");
})

$(".fa.fa-italic").click(function () {
    $(".handsontable td.area").css("font", "italic");
})


$(".fa.fa-font").click(function () {
    var color = $(".jscolor").val();
    $(".handsontable td.area").css("color", "#" + color);
})

$(".fa.fa-paint-brush").click(function () {
    // console.log();
    var color = $(".jscolor").val();
    $(".handsontable td.area").css("background-color", "#" + color);
    // changeBackgroundColor();
})

$(document).ready(
    function () {
        $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">Untitled</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

        sheetNames.push('Untitled');
        bindSheetListener();
    }
);

var bookDialog = $("#book-dialog");
$("#importTable").click(function () {
    bookDialog.empty();
    $(function () {
        $("<input type='file' name='File Upload' id='txtFileUpload' accept='.csv' />").appendTo(bookDialog);
        var dialog = bookDialog.dialog({
            buttons: {
                Confirm: function () {
                    var uploadedFile = $("#txtFileUpload").prop('files')[0];
                    if (uploadedFile != undefined) {
                        console.log(uploadedFile);
                        var filereader = new FileReader();
                        filereader.readAsArrayBuffer(uploadedFile);
                        filereader.onloadend = function (e) {
                            console.log(filereader.result);
                            var bytes = new Uint8Array(filereader.result);
                            console.log(bytes)
                            $.ajax({
                                url: baseUrl + "importBook",
                                method: "POST",
                                headers: {
                                    "auth-token": "guest"
                                },
                                data: bytes,
                                processData: false,
                                contentType: 'application/octet-stream',
                            }).done(function (result) {
                                console.log(result);
                                // $("#tableName").text(newBook);
                                // bId = result['data']['book']['id'];
                                // bName = result['data']['book']['name'];
                                if (result.status == "success") {
                                    bId = result.data.book.id;
                                    //sName = result.data.sheetName;
                                    $.get(baseUrl + "getSheets/" + bId, function (data) {
                                        $('#sheets').empty();
                                        //add sheet name to tabs
                                        var sheets = data['data']['sheets'];
                                        sName = sheets[0].name;

                                        // $('#sheets').empty();
                                        sheets.forEach(function (e) {

                                            $('<div class="btn-group dropup sheet"><button type="button" class="btn btn-secondary">' + e.name + '</button><button type="button" class="btn btn-secondary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="sr-only">Toggle Dropdown</span></button><div class="dropdown-menu"><a class="dropdown-item renameSheet" href=“#">Rename</a><a class="dropdown-item duplicateSheet" href="#">Duplicate</a><a class="dropdown-item deleteSheet" href="#">Delete</a><a class="dropdown-item clearSheet" href="#">Clear</a><a class="dropdown-item moveLeftSheet" href="#">Move Left</a><a class="dropdown-item moveRightSheet" href="#">Move Right</a></div></div>').appendTo($("#sheets"));

                                        });

                                        bindSheetListener();

                                        $("#tableName").text($('#book-selector').val());
                                        sheetData = data;

                                        importSheet(bId, sName, 0);

                                    })
                                    dialog.dialog('close');
                                } else {
                                    alert(result.message);
                                }

                            })

                        };
                    } else {
                        alert("file not uploaded");
                    }


                },
                Cancel: function () {
                    dialog.dialog('close');
                }
            }
        })
    })
});


var importSheet = function (bookId, sheetName, sheetIndex) {

    var r1 = 0;
    var c1 = 0;
    var r2 = 1000;
    var c2 = 50;
    sName = sheetName;

    $.get(baseUrl + "getCells/" + bookId + "/" + sheetName + "/" + r1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {
        //Todo check return success or failure
        var testingarray = [];
        //console.log(data)
        for (let i = r1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                if (data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value != "null") {
                    temp.push(data.data.cells[(i - r1) * (c2 - c1 + 1) + (j - c1)].value);
                } else {
                    temp.push("");
                }

            }
            testingarray.push(temp);
        }
        clearCanvas(testingarray);
    })

}

$(window).resize(function () {
    console.log("resized")
    wrapperHeight = $(".wrapper").height();
    wrapperWidth = $(".wrapper").width();
    let leftWidth = $("#navChart").width();
    if (exploreOpen) {
        hot.updateSettings({
            width: wrapperWidth - leftWidth,
            height: wrapperHeight * 0.95,
        });
        // nav.updateSettings({
        //     width: wrapperWidth * 0.19,
        //     height: wrapperHeight * 0.95,
        // })

    } else {
        hot.updateSettings({
            width: $(".wrapper").width(),
            height: $(".wrapper").height(),
        });
    }
});


//load data in spreadsheet on demand
var updateData = function (r1, c1, r2, c2, scrollTo) {
    //   clearCanvas();

    //sName = sheetName;
    if (r1 < 0) {
        r1 = 0;
    }
    let temp1 = ((r1 - 30) < 0) ? r1 : r1 - 30;


    $.get(baseUrl + "getCells/" + bId + "/" + sName + "/" + temp1 + "/" + c1 + "/" + r2 + "/" + c2, function (data) {

        // data['data']['cells'].forEach(function(e){
        //   if(e.value!=='null'){
        //     hot.setDataAtCell(e.row, e.col, e.value);
        //   }
        // })
        var testingarray = [];
        for (let i = temp1; i <= r2; i++) {
            let temp = []
            for (let j = c1; j < c2; j++) {
                temp.push(data.data.cells[(i - temp1) * (c2 - c1 + 1) + (j - c1)].value);
            }
            testingarray.push(temp);
        }
        // for(let i = 0; i < 100; i++){
        //   for (let j = 0; j < 10; j++){
        //      hot.setDataAtCell(i, j, testingarray[i][j]);
        //   }
        // }
        hot.populateFromArray(temp1, c1, testingarray);
        if (scrollTo) {
            hot.scrollViewportTo(r1);
        }
    })

}



