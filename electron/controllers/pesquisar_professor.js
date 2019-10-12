// configuração do firebase
async function carregarFirebase() {
    const firebaseConfig = {
        apiKey: "AIzaSyBAPrm6tJa59ZRLFGYQz8WHl0OGjYvlmGk",
        authDomain: "tcc-marcos-willian.firebaseapp.com",
        databaseURL: "https://tcc-marcos-willian.firebaseio.com",
        projectId: "tcc-marcos-willian",
        storageBucket: "tcc-marcos-willian.appspot.com",
        messagingSenderId: "322836423260",
        appId: "1:322836423260:web:d52dbe874173e8c1"
    };
    await firebase.initializeApp(firebaseConfig)
}


//máscara para o CPF
$("#inputPesquisaCPFProfessor").mask("000.000.000-00");


// pesquisa do professor no firebase
function pesquisar() {
    $('#resultadoPesquisaProfessor tbody').remove()
    const inputNome = document.getElementById('inputPesquisaNomeProfessor').value
    const cpfEntrada = document.getElementById('inputPesquisaCPFProfessor').value
    const cpfFormatado = retirarCaracteresEspeciais(cpfEntrada)
    const nomeMinusculo = inputNome.toLowerCase()

    // criar referência para o firebase
    const banco_dados = firebase.database()
    const referencia = banco_dados.ref("usuarios/professor/")


    if (cpfFormatado == '') {

        // pesquisa de professor por nome
        referencia.orderByChild('nomeLowerCase').startAt(nomeMinusculo).endAt(`${nomeMinusculo}\uf8ff`).on('child_added', function(snapshot) {
            
            banco_dados.ref(`usuarios/professor/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()

                var novaLinha = $("<tbody><tr>")
                var colunas = ""
                colunas += '<td>' + ((resultado.nome == '')?'nome indefinido':resultado.nome) + '</td>'
                colunas += '<td>' + ((resultado.email == '')?'email indefinido':resultado.email) + '</td>'
                colunas += '<td>' + resultado.cpf + '</td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-primary" value="Editar" onClick="editar(id)"/></td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-positive" value="Apagar" onClick="deletar(id)" /></td></tr></tbody>'

                novaLinha.append(colunas);
                $('#resultadoPesquisaProfessor').append(novaLinha)

            })
        })
    } else if (inputNome == '') {

        // pesquisa de professor por CPF
        referencia.orderByChild("cpf").startAt(cpfFormatado).endAt(`${cpfFormatado}\uf8ff`).on('child_added', function(snapshot) {
            
            banco_dados.ref(`usuarios/professor/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()

                var novaLinha = $("<tbody><tr>")
                var colunas = ""
                colunas += '<td>' + ((resultado.nome == '')?'nome indefinido':resultado.nome) + '</td>'
                colunas += '<td>' + ((resultado.email == '')?'email indefinido':resultado.email) + '</td>'
                colunas += '<td>' + resultado.cpf + '</td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-primary" value="Editar" onClick="editar(id)"/></td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-positive" value="Apagar" onClick="deletar(id)"/></td></tr></tbody>'

                novaLinha.append(colunas);
                $('#resultadoPesquisaProfessor').append(novaLinha);
            })
        })
    } else if ((cpfFormatado != '') && (inputNome != '')) {

        // pesquisa de professor por CPF
        referencia.orderByChild("cpf").startAt(cpfFormatado).endAt(`${cpfFormatado}\uf8ff`).on('child_added', function(snapshot) {
            
            banco_dados.ref(`usuarios/professor/${snapshot.key}`).once('value').then(function(snapshot) {
                const resultado = snapshot.val()

                var novaLinha = $("<tbody><tr>")
                var colunas = ""
                colunas += '<td>' + ((resultado.nome == '')?'nome indefinido':resultado.nome) + '</td>'
                colunas += '<td>' + ((resultado.email == '')?'email indefinido':resultado.email) + '</td>'
                colunas += '<td>' + resultado.cpf + '</td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-primary" value="Editar" onClick="editar(id)"/></td>'
                colunas += '<td><input type="button" id="' + snapshot.key +
                    '" class="btn btn-positive" value="Apagar" onClick="deletar(id)"/></td></tr></tbody>'

                novaLinha.append(colunas);
                $('#resultadoPesquisaProfessor').append(novaLinha);
            })
        })
    } else if ((cpfFormatado == '') && (inputNome == '')) {
        alert('Nenhum parâmetro de pesquisa foi adicionado!')
    }
}


//retirar caracteres especiais do CPF
function retirarCaracteresEspeciais(cpfEntrada) {
    const cpf01 = cpfEntrada.replace('.', '')
    const cpf02 = cpf01.replace('.', '')
    const cpfSaida = cpf02.replace('-', '')
    return cpfSaida
}


// deletar usuario do firebase
function deletar(id) {
    const referencia = firebase.database().ref(`usuarios/professor/${id}`)

    referencia.remove()
        .then(function () {
            alert('Usuário apagado com sucesso!')
            window.location.reload()
        })
        .catch(function (error) {
            alert('Erro ao apagar o usuário!')
        })
}


// abrir janela para edição de usuario
function editar(id) {
    window.open('./editar_professor.html?id=' + id, 'Editar professor')
    return false
}