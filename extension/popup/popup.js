const sendMessageId = document.getElementById("make");
if (sendMessageId) {
  sendMessageId.onclick = function () {
    chrome.tabs.query({ active: true, currentWindow: true }, function (tabs) {
      chrome.tabs.sendMessage(
        tabs[0].id,
        {
          tabId: tabs[0].id,
          cmd: "make"
        },
        function (response) {
          console.log(response);
        }
      );
    });
  };
}
