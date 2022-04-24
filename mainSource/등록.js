window.addEventListener('DOMContentLoaded', function() {
	//메인으로 이동하기
	var directBtn = document.getElementsByClassName('direct_btn')[0];
	directBtn.addEventListener('click', function() {
		if (confirm('작성하던 글은 모두 사라집니다. 이동하시겠습니까?'))
			location.href = '/main';
	});
	//수정 혹은 등록 중 a 태그 클릭 시, 사용자에게 물어보기
	var aTags = document.getElementsByTagName('a');
	for (var i = 0; i < aTags.length; i++) {
		aTags[i].addEventListener('click', function(event) {
			if (!confirm('작성하던 글은 모두 사라집니다. 이동하시겠습니까?')) {
				event.preventDefault();
			}
		})
	}

	//파일 업로드 관련
	var preview = document.getElementsByClassName('preview')[0];//미리 보기를 지원할 공간
	var fileController = document.getElementsByClassName('file_controller')[0];//테스트.jpg 등 파일명이 올라가고, 삭제 기능을 지원할 공간
	var inputContainer = document.getElementsByClassName('input_container')[0];//input_file이 숨어 들어갈 공간
	var fileUpload = document.getElementsByClassName('file_upload')[0];
	var iCount = 0; //프리뷰 작업을 진행하는 loadend 이벤트에서는 반드시 -1로 현재 iCount에서 작업하도록 해야 한다. 또한 fileArr의 idx 역할도 한다.
	var fileArr = new Array(10); //input_file의 순서를 기억할 자리    
	var max = 0;

	//파일 업로드 클릭 시
	fileUpload.addEventListener('click', function() {
		console.log('흐름 순서1');
		if (max > 9) {//max>10인 경우, 11개까지 파일 업로드 창이 눌리고, 업로드 시 오류를 띄우게 되므로 9로 하는 게 맞다. click으로 max가 늘어나는 게 아니므로.
			alert('최대 10개까지 업로드할 수 있습니다.');
			return false;
		} else if (iCount > 9) {//한 번의 파일 업로드에 1개씩, 총 10번을 시도하고 1개의 파일을 삭제했을 때, 이 업로드 input의 자리는 비게 된다. 1개의 파일을 더 넣을 수 있음. 따라서 이 자리를 표시할 수 있어야 함.
			for (var i = 0; i < 10; i++) {
				if (fileArr[i] === undefined) {
					iCount = i; //빈 자리 찾아서 넣어주기. 흐름순서2로 가서 ++가 되므로 그냥 i를 꽂아주면 된다. 흐룸 순서3,4에서 iCount -1로 계산하니 상관 없음. 
				}
				else {
					alert('최대 10개까지 업로드할 수 있습니다.');
					return false;
				}
			}
		}

		if (iCount != 0) {
			if (fileArr[iCount - 1] === undefined) { //next iCount를 가리키고 있으므로 -1을 해서 이전의 iCount의 length를 확인해야 함.           
				var empty1 = inputContainer.children[iCount - 1]; //현재 iCount-1의 Arr에서, Arr.length가 0이면 사용자가 취소를 누른 것이므로 해당 태그를 삭제해준다.
				var empty2 = preview.children[iCount - 1];
				var empty3 = fileController.children[iCount - 1];
				empty1.remove();
				empty2.remove();
				empty3.remove();
				iCount--;
			}
		}

		var fileInput = document.createElement('input');
		fileInput.setAttribute('type', 'file');
		fileInput.setAttribute('multiple', true);
		fileInput.setAttribute('name', 'file[]');
		fileInput.setAttribute('accept', '.gif, .jpg, .jpeg, .png, .zip, .7z, .docx,. doc, .hwp, .xlsx, .xls, .mp3, .wav, .flac, .wma');
		fileInput.setAttribute('class', '');
		fileInput.style.display = 'none';
		inputContainer.append(fileInput);

		var inputUl1 = document.createElement('ul');
		inputUl1.setAttribute('class', 'input' + iCount);


		var inputUl2 = document.createElement('ul');
		inputUl2.setAttribute('class', 'input' + iCount);
		fileController.append(inputUl1);
		preview.append(inputUl2);

		fileInput.click();

		//파일이 업로드되는 순간, del 버튼 업데이트가 완료되어야 함. 
		fileInput.addEventListener('change', function(event) {
			console.log('흐름 순서 3');
			fileArr[iCount - 1] = new Array(fileInput.files.length);
			max += fileInput.files.length;//파일 업로드 제한 10개.

			for (let i = 0; i < fileInput.files.length; i++) {

				//파일 업로드 제약 조건
				if (fileInput.files[i].size > 1024 * 1024 * 20) { //최대 20MB 
					alert('파일 1개의 용량은 20MB를 초과할 수 없습니다.');
					fileArr[iCount - 1] = undefined; //용량 초과 시 핵심. 이것을 통해 다시 파일 업로드를 눌렀을 때, 덮어쓸 수 있다.
					return false;
				} else if (max > 10) {
					alert('최대 10개까지 업로드할 수 있습니다.');
					max -= fileInput.files.length;
					fileArr[iCount - 1] = undefined; //용량 초과 시 핵심. 이것을 통해 다시 파일 업로드를 눌렀을 때, 덮어쓸 수 있다.
					return false;
				}
				//~파일 업로드 제약 조건 끝

				var filename = fileInput.files[i].name;
				var fileType = getFileType(filename);

				if (fileType === '.jpg' || fileType === '.png' || fileType === '.jpeg' || fileType === '.gif') {
					//이미지 파일 처리

					var reader = new FileReader();
					reader.addEventListener('loadend', function(event) {
						console.log('흐름 순서 5') //여기서는 흐름4의 next iCount값을 가져오게 되므로 -1을 해줘야 함.
						var img = document.createElement('img');
						img.setAttribute('src', event.target.result);

						var li = document.createElement('li');
						li.setAttribute('id', iCount - 1 + '_' + i);//미리보기 삭제를 위한 id
						li.append(img);

						var del1 = document.createElement('span');
						del1.setAttribute('class', 'del1');
						del1.style.float = 'left';
						del1.style.fontWeight = 'bolder';
						del1.textContent = 'ⓧ';
						li.append(del1);//미리 보기에서 삭제 요청

						//프리뷰 파일에 업로드 내용 수정 기능 추가.
						del1.addEventListener('click', function() {
							var nums = this.parentElement.getAttribute('id').split('_');
							fileArr[nums[0]][nums[1]] = false;
							document.getElementById(nums[0] + '__' + nums[1]).remove();//컨트롤러 삭제
							if (!this.parentElement.parentElement.hasChildNodes) {
								this.parentElement.parentElement.remove();//ul이 비었으면 아예 삭제해버리기.
								fileArr[nums[0]] = undefined; //공실로 만들어버리기.
							}
							else
								this.parentElement.remove();//만약 ul에 아직 li가 남아 있다면 li만 삭제.(프리뷰 삭제)
							max--;
						});
						//~업로드 내용 수정 완료

						//sortedInsert(preview.children[iCount - 1], li);
						sortedInsert2(preview.children[iCount - 1], li);
						fileArr[iCount - 1][i] = true;//파일 컨트롤, 서버 전송 컨트롤을 위한 배열 작업
					});
					console.log('흐름 순서 4')//여기서는 아직 흐름1의 iCount를 가져오게 된다.
					var li = document.createElement('li');
					li.setAttribute('id', iCount - 1 + '__' + i);//컨트롤러에서 삭제 요청을 하면, 이 아이디를 통해 배열에 접근하고, 배열에 0을 저장한다. 그리고 이미지/오디오면 해당 프리뷰도 삭제한다.
					li.textContent = filename;

					var del2 = document.createElement('span');
					del2.textContent = ' ⓧ';
					del2.setAttribute('class', 'del2');
					del2.style.fontWeight = 'bolder';
					li.append(del2)//컨트롤러에서 삭제 요청

					//컨트롤러에 업로드 내용 수정 기능 추가
					del2.addEventListener('click', function() {
						var nums = this.parentElement.getAttribute('id').split('__');
						fileArr[nums[0]][nums[1]] = false;
						document.getElementById(nums[0] + '_' + nums[1]).remove();//프리뷰 삭제
						if (!this.parentElement.parentElement.hasChildNodes) {
							this.parentElement.parentElement.remove();//ul이 비었으면 아예 삭제해버리기.
							fileArr[nums[0]] = undefined; //공실로 만들어버리기.
						}
						else
							this.parentElement.remove();//만약 ul에 아직 li가 남아 있다면 li만 삭제.(컨트롤러 삭제)
						max--;
					});
					//~업로드 내용 수정 완료

					fileController.children[iCount - 1].append(li);

					reader.readAsDataURL(fileInput.files[i]);
				} else if (fileType === '.mp3' || fileType === '.wav' || fileType === '.flac' || fileType === '.wma') {
					//오디오 파일 처리

					var reader = new FileReader();

					reader.addEventListener('loadend', function(event) {
						var audio = document.createElement('audio');
						audio.setAttribute('src', event.target.result);
						audio.setAttribute('controls', true);

						var li = document.createElement('li');
						li.setAttribute('id', iCount - 1 + '_' + i);//미리보기 삭제를 위한 id
						li.append(audio);

						var del1 = document.createElement('span');
						del1.setAttribute('class', 'del1');
						del1.style.float = 'left';
						del1.style.fontWeight = 'bolder';
						del1.textContent = 'ⓧ';
						li.append(del1);

						//프리뷰 파일에 업로드 내용 수정 기능 추가.
						del1.addEventListener('click', function() {
							var nums = this.parentElement.getAttribute('id').split('_');
							fileArr[nums[0]][nums[1]] = false;
							document.getElementById(nums[0] + '__' + nums[1]).remove();//컨트롤러 삭제
							if (!this.parentElement.parentElement.hasChildNodes) {
								this.parentElement.parentElement.remove();//ul이 비었으면 아예 삭제해버리기.
								fileArr[nums[0]] = undefined; //공실로 만들어버리기.
							}
							else
								this.parentElement.remove();//만약 ul에 아직 li가 남아 있다면 li만 삭제.(프리뷰 삭제)
							max--;
						});
						//~업로드 내용 수정 완료

						//sortedInsert(preview.children[iCount - 1], li);
						sortedInsert2(preview.children[iCount - 1], li);
						fileArr[iCount - 1][i] = true;//파일 컨트롤, 서버 전송 컨트롤을 위한 배열 작업
					});
					var li = document.createElement('li');
					li.setAttribute('id', iCount - 1 + '__' + i);
					li.textContent = filename;

					var del2 = document.createElement('span');
					del2.textContent = ' ⓧ';
					del2.setAttribute('class', 'del2');
					del2.style.fontWeight = 'bolder';
					li.append(del2)//컨트롤러에서 삭제 요청

					//컨트롤러에 업로드 내용 수정 기능 추가
					del2.addEventListener('click', function() {
						var nums = this.parentElement.getAttribute('id').split('__');
						fileArr[nums[0]][nums[1]] = false;
						document.getElementById(nums[0] + '_' + nums[1]).remove();//프리뷰 삭제
						if (!this.parentElement.parentElement.hasChildNodes) {
							this.parentElement.parentElement.remove();//ul이 비었으면 아예 삭제해버리기.
							fileArr[nums[0]] = undefined; //공실로 만들어버리기.
						}
						else
							this.parentElement.remove();//만약 ul에 아직 li가 남아 있다면 li만 삭제.(컨트롤러 삭제)
						max--;
					});
					//~업로드 내용 수정 완료
					fileController.children[iCount - 1].append(li);

					reader.readAsDataURL(fileInput.files[i]);
				} else if (fileType === '.zip' || fileType === '.docx' || fileType === '.doc' || fileType === '.hwp' || fileType === '.xlsx' || fileType === '.xls' || fileType === '.7z') {
					//일반 파일 처리
					var li = document.createElement('li');
					li.setAttribute('id', iCount - 1 + '__' + i);
					li.textContent = filename;

					var del2 = document.createElement('span');
					del2.textContent = ' ⓧ';
					del2.setAttribute('class', 'del2');
					del2.style.fontWeight = 'bolder';
					li.append(del2)//컨트롤러에서 삭제 요청

					//컨트롤러에 업로드 내용 수정 기능 추가
					del2.addEventListener('click', function() {
						var nums = this.parentElement.getAttribute('id').split('__');
						fileArr[nums[0]][nums[1]] = false;
						if (!this.parentElement.parentElement.hasChildNodes) {
							this.parentElement.parentElement.remove();//ul이 비었으면 아예 삭제해버리기.
							fileArr[nums[0]] = undefined; //공실로 만들어버리기.
						}
						else
							this.parentElement.remove();//만약 ul에 아직 li가 남아 있다면 li만 삭제.(컨트롤러 삭제)
						max--;
					});
					//~업로드 내용 수정 완료

					fileController.children[iCount - 1].append(li);
					fileArr[iCount - 1][i] = true;//파일 컨트롤, 서버 전송 컨트롤을 위한 배열 작업
				} else {
					alert('올바른 파일을 선택해주세요.');
				}
			}
		});
		iCount++;//next idx를 가리키도록 만든다. 이제 흐름 순서도대로 따라갈 것.
		console.log('흐름 순서 2');
	});
	//~파일 업로드 종료

	//유튜브 작업    
	var ytController = document.getElementsByClassName('yt_controller')[0];//iframe 조작을 위함
	var videoContainer = document.getElementsByClassName('video_container')[0];
	var th = 0; //중복 확인용 및 삭제
	var ids = [];//중복 확인용, 최종 전송 시 서버로 전달할 아이디 목록
	var ytBtn = document.getElementsByClassName('yt_btn')[0];
	var ytCancle = document.getElementsByClassName('link_reg_cancle')[0];
	var ytInputBtn = document.getElementsByClassName('link_reg_btn')[0];
	var ytLink = document.getElementsByName("yt_link")[0];

	ytInputBtn.addEventListener('click', function() {
		if (max > 9) {//max>10인 경우, 11개까지 파일 업로드 창이 눌리고, 업로드 시 오류를 띄우게 되므로 9로 하는 게 맞다. click으로 max가 늘어나는 게 아니므로.
			alert('최대 10개까지 업로드할 수 있습니다.');
			return false;
		}

		if (max <= 10) {//이 조건문은 없애도 된다.
			var ytId = getYtId(ytLink.value); //입력된 유튜브 url에서 아이디 추출하기.
			if (ytId != false) {
				if (ids.indexOf(ytId) == -1) {
					//iframe 생성
					ids[th] = ytId;
					//유튜브 미리보기 삭제 도구 생성
					var del3 = document.createElement('span');
					del3.textContent = ' ⓧ';
					del3.setAttribute('class', 'del3');
					del3.style.fontWeight = 'bolder';
					del3.style.float = 'left';
					del3.addEventListener('click', function() {
						var idx = getIdx(videoDel);
						videoContainer.removeChild(videoContainer.children[idx]);
						ytController.removeChild(ytController.children[idx]); //영상1, 영상2, ... 지정된 영상i 떼어내기.
						th = ytController.childElementCount; //영상 12345 중 3이 빠졌으니, 1234가 되고 다음에 저장될 영상을 위해 th는 4를 가리키고 있어야 한다.

						//영상1, 영상2, 영상3, 영상4, 영상5에서 영상 3을 빼버리면 영상4,5를 3,4로 바꿔줘야 한다. 이 작업.
						for (var i = idx; i < th; i++) {
							ytController.children[i].textContent = '유튜브 영상' + (i + 1) + ' ⓧ';
						}

						//ids에 저장된 id 배열에서 삭제하려는 영상의 아이디만 빼고 새로 저장. 
						ids = ids.filter(function(id) {
							return id !== ids[idx];
						});
						max--;
					});
					//~미리보기 삭제 도구 생성 완료
					var frame = document.createElement('iframe');
					frame.setAttribute('id', 'player');
					frame.setAttribute('class', 'slide_video');
					frame.style.width = '658px'; //16:9 스타일. 바꿀 거면 계산해서 여기 바꾸자
					frame.style.height = '370px'; //16:9 스타일. 바꿀 거면 계산해서 여기 바꾸자
					frame.setAttribute('src', 'https://www.youtube.com/embed/' + ytId + '?enablejsapi=1&autoplay=0&mute=1&controls=1&loop=1&playlist=' + ytId)

					//미리보기와 미리보기 삭제 도구를 담을 div 생성
					var div = document.createElement('div');
					div.append(del3);
					div.append(frame);
					//생성 완료

					videoContainer.append(div);

					//컨트롤러 삭제 도구 생성
					var videoDel = document.createElement('span');
					videoDel.textContent = '유튜브 영상' + (th + 1) + ' ⓧ';
					videoDel.setAttribute('class', 'link_list ' + (th + 1));
					ytController.append(videoDel);
					videoDel.addEventListener('click', function() {
						var idx = getIdx(videoDel);
						videoContainer.removeChild(videoContainer.children[idx]);
						ytController.removeChild(ytController.children[idx]); //영상1, 영상2, ... 지정된 영상i 떼어내기.
						th = ytController.childElementCount; //영상 12345 중 3이 빠졌으니, 1234가 되고 다음에 저장될 영상을 위해 th는 4를 가리키고 있어야 한다.

						//영상1, 영상2, 영상3, 영상4, 영상5에서 영상 3을 빼버리면 영상4,5를 3,4로 바꿔줘야 한다. 이 작업.
						for (var i = idx; i < th; i++) {
							ytController.children[i].textContent = '유튜브 영상' + (i + 1) + ' ⓧ';
						}

						//ids에 저장된 id 배열에서 삭제하려는 영상의 아이디만 빼고 새로 저장. 
						ids = ids.filter(function(id) {
							return id !== ids[idx];
						});
						max--;
					});
					//컨트롤러 삭제 도구 생성 완료
					th++;
				} else
					alert('이미 등록된 영상입니다.');
			}
			ytLink.value = null;
		} else
			alert('파일은 최대 10개까지 업로드할 수 있습니다.');
	});

	ytCancle.addEventListener('click', function() {
		var ytOption = document.getElementById('yt_option');
		ytOption.setAttribute('class', 'hidden');
		ytLink.value = null;
	});

	ytBtn.addEventListener('click', function() {
		var ytOption = document.getElementById('yt_option');
		ytOption.setAttribute('class', '');
	});
	//~유튜브 작업 완료

	//최종 게시글 등록1
	var params = new URLSearchParams(location.search)
	var board = params.get("board");

	var submitBtn = document.getElementById('submit_btn'); //이클립스 상에서는 이름 바꾸자.
	var filesToServer = document.getElementById('files_to_server');
	submitBtn.addEventListener('click', function() {
		filesToServer.value = ''; //이전에 한 번 클릭했었으면 전부 삭제.
		for (var i = 0; i < ids.length; i++) {
			filesToServer.value += (ids[i] + '.yt,');//구분자는 .yt로 하자. 
		}

		if (submitCheck()) {
			var formData = new FormData();
			//업로드할 파일 골라내기 시작
			for (var i = 0; i < inputContainer.childElementCount; i++) {
				if (fileArr[i] !== undefined) {//삭제되어 공실이 된 input이 아니라면
					for (var j = 0; j < fileArr[i].length; j++) {//해당 input files의 길이만큼 돌면서 모든 내용을 복사한다.
						if (fileArr[i][j] === true) {
							formData.append('files', inputContainer.children[i].files[j]);
							filesToServer.value += (inputContainer.children[i].files[j].name + ",");//파일 이름 저장
						}
					}
				}
			}
			//~파일 골라내기 끝

			//파일 업로드 시작
			fileUpload = getXMLHttpRequest();
			fileUpload.open("POST", "/board/fileUpload", true);
			fileUpload.onreadystatechange = function() {
				if (fileUpload.readyState === 4) {
					if (fileUpload.status === 200) {
						var resultText1 = fileUpload.responseText;
						if (resultText1 != -1 && resultText1 != 0) {
							var content = document.getElementById('content_area').value;
							var title = document.getElementById('input_title').value;

							//게시글 등록
							httpRequest = getXMLHttpRequest();
							httpRequest.open('POST', '/board/reg')
							httpRequest.setRequestHeader('content-Type', 'application/x-www-form-urlencoded');
							httpRequest.onreadystatechange = function() {
								if (httpRequest.readyState === 4) {
									if (httpRequest.status === 200) {
										var resultText2 = httpRequest.responseText;
										if (resultText2 != 0 && resultText2 != -1) {
											window.location.href = '/board/detail?board=' + board + '&id=' + resultText2 + '&p=1';
										} else if (resultText2 == 0)
											alert('서버와의 통신이 실패하였습니다.');
										else {
											alert('서버와의 통신 중 오류가 발생하였습니다.')
										}
									} else
										alert('서버와의 통신 중 오류가 발생하였습니다.');
								}
							};

							httpRequest.send('board=' + board + '&content=' + content + '&title=' + title + '&files=' + filesToServer.value + '&tmp=' + resultText1);
							//~게시글 등록 끝
						}
						else if (resultText1 == -1)
							alert('잘못된 파일 형식입니다.');
						else if (resultText1 == 0)
							alert('서버와의 통신이 실패하였습니다.');
						else
							alert('알 수 없는 오류가 발생하였습니다.');
					} else
						alert('서버와의 통신 중 오류가 발생하였습니다.');
				}
			};
			fileUpload.send(formData);
			//~파일 업로드 끝
		}
	});
	//~게시글 등록 완료1
});


function cancle() {
	if (confirm("게시글 작성을 취소하시겠어요?"))
		window.history.back()
}

function submitCheck() {
	var title = document.getElementById("input_title");
	var content = document.getElementById("content_area");

	if (title.value === "" || title.value === null) {
		alert("게시글 제목을 입력하세요");
		return false;
	}

	if (content.value === "" || content.value === null) {
		alert("내용을 입력하세요");
		return false;
	}

	var lastCheck = confirm("게시판에 글을 등록하시겠어요?");

	if (lastCheck) return true;
	else return false;
};


function getFileType(filename) {
	var fileLen = filename.length;
	var distinguisher = filename.lastIndexOf('.');

	// 확장자 명만 추출한 후 소문자로 변경
	var fileType = filename.substring(distinguisher, fileLen).toLowerCase();
	return fileType;
}

//가장 작은 것부터 세기. 이미지는 웬만하면 작은 것 순서대로 들어가게 되므로 미약하지만 조금 더 자원을 사용함. 대신 코드가 si2보다는 짧다.
function sortedInsert(parentUl, children) {
	/*    
	(1) 맨 처음 들어가는 children
	(2) children이 가장 클 때, i = length-1일 때까지 모두 돌았지만 자리를 못 찾은 경우(for 내부에 넣으면 자원 낭비 바깥으로) append


	1. li가 아무것도 없으면 일단 append
	2. 가장 작은 li부터 차례대로 비교해가며 자기보다 큰 li를 발견하면 그 li 바로 앞에 자리 잡기.
	3. 위의 작업을 반복. */
	if (!parentUl.hasChildNodes())
		parentUl.append(children); //(1)
	else {
		var childrenNth = children.getAttribute('id').split('_')[1];
		var length = parentUl.childElementCount;
		var orderClear = false;
		for (var i = 0; i < length; i++) {
			var targetNth = parentUl.children[i].getAttribute('id').split('_')[1];
			//자리 찾기, 0 4 9가 들어 있는 ul에, children7이 들어갈 자리는 4와 9 사이임. 0과 4는 전부 7보다 작으니 통과하고, 마지막 9에서 걸려 insertBefore 9
			if (parseInt(targetNth) > parseInt(childrenNth)) {
				parentUl.insertBefore(children, parentUl.children[i]);
				orderClear = true;
				break;
			}
		}
		//자리 찾기 실패. 집어 넣으려는 children이 가장 큼. 맨 뒤에 삽입.(2)
		if (orderClear === false)
			parentUl.append(children);
	}
}


//가장 큰 것부터 세기, 자원 절약 가능
function sortedInsert2(parentUl, children) {
	/*    
	(1) 맨 처음 들어가는 children
	(2) children이 가장 작을 때, i = 0일 때까지 모두 돌았지만 자리를 못 찾은 경우(for 내부에 넣으면 자원 낭비 바깥으로) prepend


	1. li가 아무것도 없으면 일단 append
	2-1. 가장 오른쪽에 있는, 즉 가장 큰 li id와 비교 후, 자기가 더 크면 그대로 append
	2-2.가장 큰 id가 자기보다 크면, 그 다음 큰 애랑 자기를 비교. 만약 내가 걔보다는 크다면, 그 이전에 비교했던 i+1 바로 앞에 집어넣자. insertAfter 구현보다는 insertBefore 그대로 이용
	2-3. 계속 비교하다가, i == 0일 때, 즉 마지막 비교일 때 i=0인 애보다도 작으면 그냥 얘 앞에 넣으면 된다. 만약 얘보다는 크면 i+1 insertBefore. 즉 추가하려는 children이 2번째 작은 애.
	3. 위의 작업을 반복. */
	if (!parentUl.hasChildNodes())
		parentUl.prepend(children); //(1)
	else {
		var childrenNth = children.getAttribute('id').split('_')[1];
		var length = parentUl.childElementCount;
		var orderClear = false;
		//가장 큰 것과 비교해서 children이 더 크면 그냥 바로 append
		if (childrenNth > parseInt(parentUl.lastChild.getAttribute('id').split('_')[1])) {
			parentUl.append(children);
			orderClear = true;
		} else {
			//가장 큰 것보다는 작으니, 여기서 확인 과정을 거쳐야 한다. insertAfter를 자체 구현하지 않았으니 if else문으로 나누어 insertBefore 사용. 
			for (var i = length - 2; i >= 0; i--) {
				var targetNth = parentUl.children[i].getAttribute('id').split('_')[1];
				//자리 찾기, 0 4 9가 들어 있는 ul에, children7이 들어갈 자리는 4와 9 사이임. 9는 7보다 크니 통과. 4보다는 크니, 4의 앞자리인 i+1, 9 앞에 삽입되어야 함.
				if (parseInt(targetNth) < parseInt(childrenNth)) {
					parentUl.insertBefore(children, parentUl.children[i + 1]);
					orderClear = true;
					break;
				}
			}
		}
		//자리 찾기 실패. 집어 넣으려는 children이 가장 작음. 맨 앞에 삽입.(2)
		if (orderClear === false)
			parentUl.prepend(children);
	}
}

function getOrder(tag) {
	var tmp = tag.getAttribute('id').split('_');
	var power = tmp[1];
	return
}

//유튜브 url에서 id 추출.
function getYtId(ytLink) {
	if (ytLink === '' || ytLink === null || ytLink === undefined) {
		alert('URL를 확인해주세요');
		return false;
	}

	//반드시 http를 포함해야 한다. 
	if (ytLink.indexOf('http') == -1) {
		alert('URL을 확인해주세요');
		return false;
	}

	var ytId;
	if (ytLink.indexOf('?v=') != -1) {
		var parsedLink = ytLink.split('v=')[1];
		var endIndex = parsedLink.indexOf('&');
		var ytId;
		if (endIndex != -1) {
			ytId = parsedLink.substring(0, endIndex);
		}
		if (ytId === undefined || ytId === null) {
			alert('URL을 확인해주세요');
			return false;
		} else {
			return ytId;
		}
	} else {
		ytId = ytLink.split('/');
		if (ytId === undefined || ytId === null) {
			alert('URL을 확인해주세요');
			return false;
		}
		return ytId[3];
	}
};


//링크 삭제용 span이 몇 번째 span인지 찾기. 여기서 찾아낸 idx를 가지고 idx'th의 iframe을 삭제한다.
function getIdx(linkDel) {
	var tmp;
	for (var i = 0; i < linkDel.parentElement.children.length; i++) {
		if (linkDel.parentElement.children[i].className === linkDel.className) {
			tmp = i;
			break;
		}
	}
	return tmp;
}

function getXMLHttpRequest() { //브라우저가 IE일 경우 XMLHttpRequest 객체 구하기 
	var httpRequest = null;
	if (window.ActiveXObject) {
		//Msxml2.XMLHTTP가 신버전이어서 먼저 시도
		try {
			return new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				return new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e1) {
				return null;
			}
		} //IE 외 파이어폭스 오페라 같은 브라우저에서 XMLHttpRequest 객체 구하기 
	} else if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
	} else {
		return null;
	}
}

