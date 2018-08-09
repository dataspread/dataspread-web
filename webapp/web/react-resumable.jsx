/**
 * Resumable JS for React JS
 * @author Gonzalo Rubino gonzalo_rubino@artear.com || gonzalorubino@gmail.com
 * @version 1.1.0
 *
 * Creates an uploader component in React, to use with Resumable JS
 * On file added, the upload will begin.
 */

class ReactResumableJs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            progressBar: 0,
            messageStatus: '',
            fileList: {files: []},
            isPaused: false,
            isUploading: false
        };

        this.resumable = null;
    }

    componentDidMount = () => {

        let ResumableField = new Resumable({
            target: this.props.service,
            query: this.props.query || {},
            fileType: this.props.filetypes,
            maxFiles: this.props.maxFiles,
            maxFileSize: this.props.maxFileSize,
            fileTypeErrorCallback: (file, errorCount) => {
                if (typeof this.props.onFileAddedError === "function") {
                    this.props.onFileAddedError(file, errorCount);
                }
            },
            maxFileSizeErrorCallback: (file, errorCount) => {
                if (typeof  this.props.onMaxFileSizeErrorCallback === "function") {
                    this.props.onMaxFileSizeErrorCallback(file, errorCount);
                }
            },
            testMethod: this.props.testMethod || 'post',
            testChunks: this.props.testChunks || false,
            headers: this.props.headerObject || {},
            chunkSize: this.props.chunkSize,
            simultaneousUploads: this.props.simultaneousUploads,
            fileParameterName: this.props.fileParameterName,
            generateUniqueIdentifier: this.props.generateUniqueIdentifier,
            forceChunkSize: this.props.forceChunkSize
        });

        if (typeof this.props.maxFilesErrorCallback === "function") {
            ResumableField.opts.maxFilesErrorCallback = this.props.maxFilesErrorCallback;
        }

        ResumableField.assignBrowse(this.uploader);

        //Enable or Disable DragAnd Drop
        if (this.props.disableDragAndDrop === false) {
            ResumableField.assignDrop(this.dropZone);
        }

        ResumableField.on('fileAdded', (file, event) => {
            this.setState({
                messageStatus: this.props.fileAddedMessage || ' Starting upload! '
            });

            if (typeof this.props.onFileAdded === "function") {
                this.props.onFileAdded(file, this.resumable);
            } else {
                ResumableField.upload();
            }
        });

        ResumableField.on('fileSuccess', (file, fileServer) => {

            if (this.props.fileNameServer) {
                let objectServer = JSON.parse(fileServer);
                file.fileName = objectServer[this.props.fileNameServer];
            } else {
                file.fileName = fileServer;
            }

            let currentFiles = this.state.fileList.files;
            currentFiles.push(file);

            this.setState({
                fileList: {files: currentFiles},
                messageStatus: this.props.completedMessage + file.fileName || fileServer
            }, () => {
                if (typeof this.props.onFileSuccess === "function") {
                    this.props.onFileSuccess(file, fileServer);
                }
            });
        });

        ResumableField.on('progress', () => {


            this.setState({
                isUploading: ResumableField.isUploading()
            });

            if ((ResumableField.progress() * 100) < 100) {
                this.setState({
                    messageStatus: parseInt(ResumableField.progress() * 100, 10) + '%',
                    progressBar: ResumableField.progress() * 100
                });
            } else {
                setTimeout(() => {
                    this.setState({
                        progressBar: 0
                    })
                }, 1000);
            }

        });

        ResumableField.on('fileError', (file, errorCount) => {
            this.props.onUploadErrorCallback(file, errorCount);
        });

        this.resumable = ResumableField;
    };

    removeFile = (event, file, index) => {

        event.preventDefault();

        let currentFileList = this.state.fileList.files;
        delete currentFileList[index];

        this.setState({
            fileList: {files: currentFileList}
        });

        this.props.onFileRemoved(file);
        this.resumable.removeFile(file);
    };

    createFileList = () => {

        let markup = this.state.fileList.files.map((file, index) => {

            let uniqID = this.props.uploaderID + '-' + index;
            let originFile = file.file;
            let media = '';

            if (file.file.type.indexOf('video') > -1) {
                media = <label className="video">{originFile.name}</label>;
                return <li className="thumbnail" key={uniqID}>
                    <label id={"media_" + uniqID}>{media}</label>
                    <a onClick={(event) => this.removeFile(event, file, index)} href="#">[X]</a>
                </li>;
            }
            else if (file.file.type.indexOf('image') > -1) if (this.props.tmpDir !== "") {
                let src = this.props.tmpDir + file.fileName;
                media = <img className="image" width="80" src={src} alt=""/>;
                return <li className="thumbnail" key={uniqID}>
                    <label id={"media_" + uniqID}>{media}</label>
                    <a onClick={(event) => this.removeFile(event, file, index)} href="#">[X]</a>
                </li>;

            } else {
                let fileReader = new FileReader();
                fileReader.readAsDataURL(originFile);
                fileReader.onload = (event) => {
                    media = '<img class="image" width="80" src="' + event.target.result + '"/>';
                    document.querySelector("#media_" + uniqID).innerHTML = media;
                };
                return <li className="thumbnail" key={uniqID}>
                    <label id={"media_" + uniqID}/>
                    <a onClick={(event) => this.removeFile(event, file, index)} href="#">[X]</a>
                </li>;
            } else {
                media = <label className="document">{originFile.name}</label>;
                return <li className="thumbnail" key={uniqID}>
                    <label id={"media_" + uniqID}>{media}</label>
                    <a onClick={(event) => this.removeFile(event, file, index)} href="#">[X]</a>
                </li>;
            }
        });

        return <ul id={"items-" + this.props.uploaderID}>{markup}</ul>;
    };

    cancelUpload = () => {
        this.resumable.cancel();

        this.setState({
            fileList: {files: []}
        });

        this.props.onCancelUpload();
    };

    pauseUpload = () => {
        if (!this.state.isPaused) {

            this.resumable.pause();
            this.setState({
                isPaused: true
            });
            this.props.onPauseUpload();
        } else {

            this.resumable.upload();
            this.setState({
                isPaused: false
            });
            this.props.onResumeUpload();
        }
    };

    startUpload = () => {
        this.resumable.upload();
        this.setState({
            isPaused: false
        });
        this.props.onStartUpload();
    };

    render() {

        let fileList = null;
        if (this.props.showFileList) {
            fileList = <div className="resumable-list">{this.createFileList()}</div>;
        }

        let previousText = null;
        if (this.props.previousText) {
            if (typeof this.props.previousText ==="string") previousText = <p>{this.props.previousText}</p>
            else previousText = this.props.previousText
        }

        let textLabel = null;
        if (this.props.textLabel) {
            textLabel = this.props.textLabel;

        }

        let startButton = null;
        if (this.props.startButton) {
            if (typeof this.props.startButton ==="string" || typeof this.props.startButton ==="boolean" ) startButton = <label>
                <button disabled={this.state.isUploading} className="btn start" onClick={this.startUpload}>{this.props.startButton && "upload"}
                </button>
            </label>;
            else startButton =this.props.startButton
        }

        let cancelButton = null;
        if (this.props.cancelButton) {
            if (typeof this.props.cancelButton ===  "string" || typeof this.props.cancelButton ===  "boolean")cancelButton = <label>
                <button disabled={!this.state.isUploading} className="btn cancel" onClick={this.cancelUpload}>{this.props.cancelButton && "cancel"}
                </button>
            </label>;
            else cancelButton = this.props.cancelButton
        }

        let pauseButton = null;
        if (this.props.pauseButton) {
            if (typeof this.props.pauseButton ===  "string" || typeof this.props.pauseButton ===  "boolean") pauseButton = <label>
                <button disabled={!this.state.isUploading} className="btn pause" onClick={this.pauseUpload}>{this.props.pauseButton && "pause"}
                </button>
            </label>;
            else pauseButton = this.props.pauseButton
        }

        return (
            <div id={this.props.dropTargetID} ref={node => this.dropZone = node}>
                {previousText}
                <label className={this.props.disableInput ? 'btn file-upload disabled' : 'btn file-upload'}>{textLabel}
                    <input
                        ref={node=> this.uploader = node}
                        type="file"
                        id={this.props.uploaderID}
                        className='btn'
                        name={this.props.uploaderID + '-upload'}
                        accept={this.props.fileAccept || '*'}
                        disabled={this.props.disableInput || false}
                    />
                </label>
                <div className="progress" style={{display: this.state.progressBar === 0 ? "none" : "block"}}>
                    <div className="progress-bar" style={{width: this.state.progressBar + '%'}}></div>
                </div>

                {fileList}
                {startButton}
                {pauseButton}
                {cancelButton}
            </div>
        );
    }
}

ReactResumableJs.defaultProps = {
    maxFiles: undefined,
    uploaderID: 'default-resumable-uploader',
    dropTargetID: 'dropTarget',
    filetypes: [],
    fileAccept: '*',
    showFileList: false,
    onUploadErrorCallback: (file, errorCount) => {
        console.log('error', file, errorCount);
    },
    onFileRemoved: function (file) {
        return file;
    },
    onCancelUpload: function () {
        return true;
    },
    onPauseUpload: function () {
        return true;
    },
    onResumeUpload: function () {
        return true;
    },
    onStartUpload: function () {
        return true;
    },
    disableDragAndDrop: false,
    fileNameServer: "",
    tmpDir: "",
    chunkSize: 4 * 1024 * 1024,
    simultaneousUploads: 3,
    fileParameterName: 'file',
    generateUniqueIdentifier: null,
    maxFilesErrorCallback: null,
    cancelButton: false,
    pause: false,
    startButton: null,
    pauseButton: null,
    previousText: "",
    headerObject : {},
    forceChunkSize: false
};