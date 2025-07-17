document.getElementById('btn-imprimir').addEventListener('click', async function (e) {
    e.preventDefault();

    const contenido = document.getElementById('factura-imprimible');
    if (!contenido) {
        console.error('No se encontró el contenido a imprimir');
        return;
    }
    contenido.style.display = 'flex';
    contenido.classList.add('d-flex');
    // Mostramos temporalmente
    contenido.style.display = 'block';
    await new Promise(resolve => setTimeout(resolve, 100)); // darle tiempo al DOM

    const canvas = await html2canvas(contenido, {
        useCORS: true,
        scale: 2
    });

    // Lo volvemos a ocultar
    contenido.style.display = 'none';
    contenido.classList.remove('d-flex');
    const imgData = canvas.toDataURL('image/jpeg', 1.0);

    // Calculá el tamaño en mm del canvas para escalarlo al tamaño A4
    const imgWidth = 210; // A4 horizontal en mm
    const imgHeight = canvas.height * imgWidth / canvas.width;

    const pdf = new jsPDF('p', 'mm', 'a4');
    pdf.addImage(imgData, 'JPEG', 0, 0, imgWidth, imgHeight);

    const pdfBlob = pdf.output('blob');
    const pdfUrl = URL.createObjectURL(pdfBlob);
    window.open(pdfUrl, '_blank');
});
