var files = [];
var path = "/";
var showSystem = false;
const getFiles = async () => {
  files = [];
  if (!["/", "D:/", "C:/"].includes(path))
    files.push({
      path: "..",
      isDir: true,
    });
  else if (showSystem) {
    if (path != "D:/")
      files.push({
        path: "D:/",
        isDir: true,
        replace: true,
      });
    if (path != "C:/")
      files.push({
        path: "C:/",
        isDir: true,
        replace: true,
      });
  }
  const url = showSystem ? "/api/system/files" : "/api/files";
  const res = await (await fetch(url + "?path=" + path)).json();
  console.log(res);
  files = files.concat(
    res
      .sort((a, b) => (a.path > b.path ? 1 : -1))
      .sort((a, b) => (a.isDir ? (b.isDir ? 0 : -1) : 1))
  );
  return files;
};

const onload = (dontupdate) => {
  const urlParams = new URLSearchParams(window.location.search);
  path = urlParams.get("path") || "/";
  showSystem = urlParams.get("system") == "true";
  //document.getElementById("system").checked = showSystem;
  reload(dontupdate);
};

window.onpopstate = () => onload(true);

const reload = async (dontupdate) => {
  if (!dontupdate)
    history.pushState(null, null, `/files?path=${path}&system=${showSystem}`);
  document.title = "Files: " + path;
  await getFiles();
  console.log(files);
  renderFiles();
};

const renderFiles = () => {
  const filesDiv = document.getElementById("files");
  filesDiv.innerHTML = "";
  console.log("a", files);
  files.forEach((file, i) => filesDiv.appendChild(transformFile(file, i)));
};

const transformFile = (file, i) => {
  console.log(file);
  const fileDiv = document.createElement("div");
  fileDiv.className = "file";
  // <div class="file-icon"><img src="/assets/${file.type}.png" /></div>

  let f = `<div class="file-name${file.isDir ? " dir" : ""}"`;
  f += ` onclick="openFile(${i})"`;
  f += `><span>${file.path}</span></div>`;
  fileDiv.innerHTML = f;

  return fileDiv;
};

const openFile = (i) => {
  let file = files[i];
  if (file.path == "..") path = path.split("/").slice(0, -2).join("/") + "/";
  else {
    if (file.isDir) path = file.replace ? file.path : path + file.path + "/";
    else {
      if (showSystem) return;
      location.search = "";
      //history.pushState(null, null, "/files?path=" + path);
      return location.replace(path.replace("/files", "") + file.path);
    }
  }

  reload();
};

function toggleSystem() {
  const checkbox = document.getElementById("system");
  showSystem = checkbox.checked;
  path = showSystem ? "C:/" : "/";
  reload();
}

// copilot wrote this one
function search() {
  var input, filter, ul, li, a, i, txtValue;
  input = document.getElementById("search");
  filter = input.value.toUpperCase();
  ul = document.getElementById("files");
  li = ul.getElementsByTagName("div");
  for (i = 0; i < li.length; i++) {
    a = li[i].getElementsByTagName("span")[0];
    txtValue = a.textContent || a.innerText;
    if (txtValue.toUpperCase().indexOf(filter) > -1) {
      li[i].style.display = "";
    } else {
      li[i].style.display = "none";
    }
  }
}
